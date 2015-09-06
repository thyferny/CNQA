
package in.thyferny.nlp.seg;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.util.*;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.collection.trie.DoubleArrayTrie;
import in.thyferny.nlp.collection.trie.bintrie.BaseNode;
import in.thyferny.nlp.corpus.tag.Nature;
import in.thyferny.nlp.dictionary.CoreDictionary;
import in.thyferny.nlp.dictionary.CustomDictionary;
import in.thyferny.nlp.dictionary.other.CharTable;
import in.thyferny.nlp.dictionary.other.CharType;
import in.thyferny.nlp.seg.NShort.Path.AtomNode;
import in.thyferny.nlp.seg.common.Term;
import in.thyferny.nlp.seg.common.Vertex;
import in.thyferny.nlp.seg.common.WordNet;
import in.thyferny.nlp.utility.Predefine;
import in.thyferny.nlp.utility.SentencesUtil;
import in.thyferny.nlp.utility.TextUtility;


public abstract class Segment
{
    
    protected Config config;

    
    public Segment()
    {
        config = new Config();
    }

    
    protected static List<AtomNode> atomSegment(char[] charArray, int start, int end)
    {
        List<AtomNode> atomSegment = new ArrayList<AtomNode>();
        int pCur = start, nCurType, nNextType;
        StringBuilder sb = new StringBuilder();
        char c;

        int[] charTypeArray = new int[end - start];

        // 生成对应单个汉字的字符类型数组
        for (int i = 0; i < charTypeArray.length; ++i)
        {
            c = charArray[i + start];
            charTypeArray[i] = CharType.get(c);

            if (c == '.' && i + start < (charArray.length - 1) && CharType.get(charArray[i + start + 1]) == CharType.CT_NUM)
                charTypeArray[i] = CharType.CT_NUM;
            else if (c == '.' && i + start < (charArray.length - 1) && charArray[i + start + 1] >= '0' && charArray[i + start + 1] <= '9')
                charTypeArray[i] = CharType.CT_SINGLE;
            else if (charTypeArray[i] == CharType.CT_LETTER)
                charTypeArray[i] = CharType.CT_SINGLE;
        }

        // 根据字符类型数组中的内容完成原子切割
        while (pCur < end)
        {
            nCurType = charTypeArray[pCur - start];

            if (nCurType == CharType.CT_CHINESE || nCurType == CharType.CT_INDEX ||
                    nCurType == CharType.CT_DELIMITER || nCurType == CharType.CT_OTHER)
            {
                String single = String.valueOf(charArray[pCur]);
                if (single.length() != 0)
                    atomSegment.add(new AtomNode(single, nCurType));
                pCur++;
            }
            //如果是字符、数字或者后面跟随了数字的小数点“.”则一直取下去。
            else if (pCur < end - 1 && ((nCurType == CharType.CT_SINGLE) || nCurType == CharType.CT_NUM))
            {
                sb.delete(0, sb.length());
                sb.append(charArray[pCur]);

                boolean reachEnd = true;
                while (pCur < end - 1)
                {
                    nNextType = charTypeArray[++pCur - start];

                    if (nNextType == nCurType)
                        sb.append(charArray[pCur]);
                    else
                    {
                        reachEnd = false;
                        break;
                    }
                }
                atomSegment.add(new AtomNode(sb.toString(), nCurType));
                if (reachEnd)
                    pCur++;
            }
            // 对于所有其它情况
            else
            {
                atomSegment.add(new AtomNode(charArray[pCur], nCurType));
                pCur++;
            }
        }

        return atomSegment;
    }

    
    protected static List<AtomNode> simpleAtomSegment(char[] charArray, int start, int end)
    {
        List<AtomNode> atomNodeList = new LinkedList<AtomNode>();
        atomNodeList.add(new AtomNode(new String(charArray, start, end - start), Predefine.CT_LETTER));
        return atomNodeList;
    }

    
    protected static List<AtomNode> quickAtomSegment(char[] charArray, int start, int end)
    {
        List<AtomNode> atomNodeList = new LinkedList<AtomNode>();
        int offsetAtom = start;
        int preType = CharType.get(charArray[offsetAtom]);
        int curType;
        while (++offsetAtom < end)
        {
            curType = CharType.get(charArray[offsetAtom]);
            if (curType != preType)
            {
                // 浮点数识别
                if (charArray[offsetAtom] == '.' && preType == CharType.CT_NUM)
                {
                    while (++offsetAtom < end)
                    {
                        curType = CharType.get(charArray[offsetAtom]);
                        if (curType != CharType.CT_NUM) break;
                    }
                }
                atomNodeList.add(new AtomNode(new String(charArray, start, offsetAtom - start), preType));
                start = offsetAtom;
            }
            preType = curType;
        }
        if (offsetAtom == end)
            atomNodeList.add(new AtomNode(new String(charArray, start, offsetAtom - start), preType));

        return atomNodeList;
    }

    
    protected static List<Vertex> combineByCustomDictionary(List<Vertex> vertexList)
    {
        Vertex[] wordNet = new Vertex[vertexList.size()];
        vertexList.toArray(wordNet);
        // DAT合并
        DoubleArrayTrie<CoreDictionary.Attribute> dat = CustomDictionary.dat;
        for (int i = 0; i < wordNet.length; ++i)
        {
            int state = 1;
            state = dat.transition(wordNet[i].realWord, state);
            if (state > 0)
            {
                int start = i;
                int to = i + 1;
                int end = - 1;
                CoreDictionary.Attribute value = null;
                for (; to < wordNet.length; ++to)
                {
                    state = dat.transition(wordNet[to].realWord, state);
                    if (state < 0) break;
                    CoreDictionary.Attribute output = dat.output(state);
                    if (output != null)
                    {
                        value = output;
                        end = to + 1;
                    }
                }
                if (value != null)
                {
                    StringBuilder sbTerm = new StringBuilder();
                    for (int j = start; j < end; ++j)
                    {
                        sbTerm.append(wordNet[j]);
                        wordNet[j] = null;
                    }
                    wordNet[i] = new Vertex(sbTerm.toString(), value);
                    i = end - 1;
                }
            }
        }
        // BinTrie合并
        if (CustomDictionary.trie != null)
        {
            for (int i = 0; i < wordNet.length; ++i)
            {
                if (wordNet[i] == null) continue;
                BaseNode<CoreDictionary.Attribute> state = CustomDictionary.trie.transition(wordNet[i].realWord.toCharArray(), 0);
                if (state != null)
                {
                    int start = i;
                    int to = i + 1;
                    int end = - 1;
                    CoreDictionary.Attribute value = null;
                    for (; to < wordNet.length; ++to)
                    {
                        if (wordNet[to] == null) continue;
                        state = state.transition(wordNet[to].realWord.toCharArray(), 0);
                        if (state == null) break;
                        if (state.getValue() != null)
                        {
                            value = state.getValue();
                            end = to + 1;
                        }
                    }
                    if (value != null)
                    {
                        StringBuilder sbTerm = new StringBuilder();
                        for (int j = start; j < end; ++j)
                        {
                            if (wordNet[j] == null) continue;
                            sbTerm.append(wordNet[j]);
                            wordNet[j] = null;
                        }
                        wordNet[i] = new Vertex(sbTerm.toString(), value);
                        i = end - 1;
                    }
                }
            }
        }
        vertexList.clear();
        for (Vertex vertex : wordNet)
        {
            if (vertex != null) vertexList.add(vertex);
        }
        return vertexList;
    }

    
    protected void mergeNumberQuantifier(List<Vertex> termList, WordNet wordNetAll, Config config)
    {
        if (termList.size() < 4) return;
        StringBuilder sbQuantifier = new StringBuilder();
        ListIterator<Vertex> iterator = termList.listIterator();
        iterator.next();
        int line = 1;
        while (iterator.hasNext())
        {
            Vertex pre = iterator.next();
            if (pre.hasNature(Nature.m))
            {
                sbQuantifier.append(pre.realWord);
                Vertex cur = null;
                while (iterator.hasNext() && (cur = iterator.next()).hasNature(Nature.m))
                {
                    sbQuantifier.append(cur.realWord);
                    iterator.remove();
                }
                if (cur != null &&
                        (cur.hasNature(Nature.q) || cur.hasNature(Nature.qv) || cur.hasNature(Nature.qt))
                        )
                {
                    if (config.indexMode)
                    {
                        wordNetAll.add(line, new Vertex(sbQuantifier.toString(), new CoreDictionary.Attribute(Nature.m)));
                    }
                    sbQuantifier.append(cur.realWord);
                    pre.attribute = new CoreDictionary.Attribute(Nature.mq);
                    iterator.remove();
                }
                if (sbQuantifier.length() != pre.realWord.length())
                {
                    pre.realWord = sbQuantifier.toString();
                    sbQuantifier.setLength(0);
                }
            }
            sbQuantifier.setLength(0);
            line += pre.realWord.length();
        }
//        System.out.println(wordNetAll);
    }

    
    public List<Term> seg(String text)
    {
        char[] charArray = text.toCharArray();
        if (MyNLP.Config.Normalization)
        {
            CharTable.normalization(charArray);
        }
        if (config.threadNumber > 1 && charArray.length > 10000)    // 小文本多线程没意义，反而变慢了
        {
            List<String> sentenceList = SentencesUtil.toSentenceList(charArray);
            String[] sentenceArray = new String[sentenceList.size()];
            sentenceList.toArray(sentenceArray);
            //noinspection unchecked
            List<Term>[] termListArray = new List[sentenceArray.length];
            final int per = sentenceArray.length / config.threadNumber;
            WorkThread[] threadArray = new WorkThread[config.threadNumber];
            for (int i = 0; i < config.threadNumber - 1; ++i)
            {
                int from = i * per;
                threadArray[i] = new WorkThread(sentenceArray, termListArray, from, from + per);
                threadArray[i].start();
            }
            threadArray[config.threadNumber - 1] = new WorkThread(sentenceArray, termListArray, (config.threadNumber - 1) * per, sentenceArray.length);
            threadArray[config.threadNumber - 1].start();
            try
            {
                for (WorkThread thread : threadArray)
                {
                    thread.join();
                }
            }
            catch (InterruptedException e)
            {
                logger.severe("线程同步异常：" + TextUtility.exceptionToString(e));
                return Collections.emptyList();
            }
            List<Term> termList = new LinkedList<Term>();
            if (config.offset || config.indexMode)  // 由于分割了句子，所以需要重新校正offset
            {
                int sentenceOffset = 0;
                for (int i = 0; i < sentenceArray.length; ++i)
                {
                    for (Term term : termListArray[i])
                    {
                        term.offset += sentenceOffset;
                        termList.add(term);
                    }
                    sentenceOffset += sentenceArray[i].length();
                }
            }
            else
            {
                for (List<Term> list : termListArray)
                {
                    termList.addAll(list);
                }
            }

            return termList;
        }
//        if (text.length() > 10000)  // 针对大文本，先拆成句子，后分词，避免内存峰值太大
//        {
//            List<Term> termList = new LinkedList<Term>();
//            if (config.offset || config.indexMode)
//            {
//                int sentenceOffset = 0;
//                for (String sentence : SentencesUtil.toSentenceList(charArray))
//                {
//                    List<Term> termOfSentence = segSentence(sentence.toCharArray());
//                    for (Term term : termOfSentence)
//                    {
//                        term.offset += sentenceOffset;
//                        termList.add(term);
//                    }
//                    sentenceOffset += sentence.length();
//                }
//            }
//            else
//            {
//                for (String sentence : SentencesUtil.toSentenceList(charArray))
//                {
//                    termList.addAll(segSentence(sentence.toCharArray()));
//                }
//            }
//
//            return termList;
//        }
        return segSentence(charArray);
    }

    
    public List<Term> seg(char[] text)
    {
        assert text != null;
        if (MyNLP.Config.Normalization)
        {
            CharTable.normalization(text);
        }
        return segSentence(text);
    }

    
    public List<List<Term>> seg2sentence(String text)
    {
        List<List<Term>> resultList = new LinkedList<List<Term>>();
        {
            for (String sentence : SentencesUtil.toSentenceList(text))
            {
                resultList.add(segSentence(sentence.toCharArray()));
            }
        }

        return resultList;
    }

    
    protected abstract List<Term> segSentence(char[] sentence);

    
    public Segment enableIndexMode(boolean enable)
    {
        config.indexMode = enable;
        return this;
    }

    
    public Segment enablePartOfSpeechTagging(boolean enable)
    {
        config.speechTagging = enable;
        return this;
    }

    
    public Segment enableNameRecognize(boolean enable)
    {
        config.nameRecognize = enable;
        config.updateNerConfig();
        return this;
    }

    
    public Segment enablePlaceRecognize(boolean enable)
    {
        config.placeRecognize = enable;
        config.updateNerConfig();
        return this;
    }

    
    public Segment enableOrganizationRecognize(boolean enable)
    {
        config.organizationRecognize = enable;
        config.updateNerConfig();
        return this;
    }

    
    public Segment enableCustomDictionary(boolean enable)
    {
        config.useCustomDictionary = enable;
        return this;
    }

    
    public Segment enableTranslatedNameRecognize(boolean enable)
    {
        config.translatedNameRecognize = enable;
        config.updateNerConfig();
        return this;
    }

    
    public Segment enableJapaneseNameRecognize(boolean enable)
    {
        config.japaneseNameRecognize = enable;
        config.updateNerConfig();
        return this;
    }

    
    public Segment enableOffset(boolean enable)
    {
        config.offset = enable;
        return this;
    }

    
    public Segment enableNumberQuantifierRecognize(boolean enable)
    {
        config.numberQuantifierRecognize = enable;
        return this;
    }

    
    public Segment enableAllNamedEntityRecognize(boolean enable)
    {
        config.nameRecognize = enable;
        config.japaneseNameRecognize = enable;
        config.translatedNameRecognize = enable;
        config.placeRecognize = enable;
        config.organizationRecognize = enable;
        config.updateNerConfig();
        return this;
    }

    class WorkThread extends Thread
    {
        String[] sentenceArray;
        List<Term>[] termListArray;
        int from;
        int to;

        public WorkThread(String[] sentenceArray, List<Term>[] termListArray, int from, int to)
        {
            this.sentenceArray = sentenceArray;
            this.termListArray = termListArray;
            this.from = from;
            this.to = to;
        }

        @Override
        public void run()
        {
            for (int i = from; i < to; ++i)
            {
                termListArray[i] = segSentence(sentenceArray[i].toCharArray());
            }
        }
    }

    
    public Segment enableMultithreading(boolean enable)
    {
        if (enable) config.threadNumber = 4;
        else config.threadNumber = 1;
        return this;
    }

    
    public Segment enableMultithreading(int threadNumber)
    {
        config.threadNumber = threadNumber;
        return this;
    }
}
