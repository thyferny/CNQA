
package in.thyferny.nlp.dependency;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.collection.trie.DoubleArrayTrie;
import in.thyferny.nlp.collection.trie.ITrie;
import in.thyferny.nlp.corpus.dependency.CoNll.CoNLLSentence;
import in.thyferny.nlp.corpus.dependency.CoNll.CoNLLWord;
import in.thyferny.nlp.corpus.io.ByteArray;
import in.thyferny.nlp.dependency.common.POSUtil;
import in.thyferny.nlp.model.bigram.BigramDependencyModel;
import in.thyferny.nlp.model.crf.CRFModel;
import in.thyferny.nlp.model.crf.FeatureFunction;
import in.thyferny.nlp.model.crf.Table;
import in.thyferny.nlp.seg.common.Term;
import in.thyferny.nlp.tokenizer.NLPTokenizer;
import in.thyferny.nlp.utility.Predefine;
import in.thyferny.nlp.utility.TextUtility;


public class CRFDependencyParser extends AbstractDependencyParser
{
    static CRFModel crfModel;
    static
    {
        long start = System.currentTimeMillis();
        if (load(MyNLP.Config.CRFDependencyModelPath))
        {
            logger.info("加载随机条件场依存句法分析器模型" + MyNLP.Config.CRFDependencyModelPath + "成功，耗时 " + (System.currentTimeMillis() - start) + " ms");
        }
        else
        {
            logger.info("加载随机条件场依存句法分析器模型" + MyNLP.Config.CRFDependencyModelPath + "失败，耗时 " + (System.currentTimeMillis() - start) + " ms");
        }
    }
    static final CRFDependencyParser INSTANCE = new CRFDependencyParser();

    public static CoNLLSentence compute(List<Term> termList)
    {
        return INSTANCE.parse(termList);
    }

    public static CoNLLSentence compute(String text)
    {
        return compute(NLPTokenizer.segment(text));
    }

    static boolean load(String path)
    {
        if (loadDat(path + Predefine.BIN_EXT)) return true;
        crfModel = CRFModel.loadTxt(path, new CRFModelForDependency(new DoubleArrayTrie<FeatureFunction>())); // 使用特化版的CRF
        return crfModel != null;
    }
    static boolean loadDat(String path)
    {
        ByteArray byteArray = ByteArray.createByteArray(path);
        if (byteArray == null) return false;
        crfModel = new CRFModelForDependency(new DoubleArrayTrie<FeatureFunction>());
        return crfModel.load(byteArray);
    }
    static boolean saveDat(String path)
    {
        try
        {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(path));
            crfModel.save(out);
            out.close();
        }
        catch (Exception e)
        {
            logger.warning("在缓存" + path + "时发生错误" + TextUtility.exceptionToString(e));
            return false;
        }

        return true;
    }
    @Override
    public CoNLLSentence parse(List<Term> termList)
    {
        Table table = new Table();
        table.v = new String[termList.size()][4];
        Iterator<Term> iterator = termList.iterator();
        for (String[] line : table.v)
        {
            Term term = iterator.next();
            line[0] = term.word;
            line[2] = POSUtil.compilePOS(term.nature);
            line[1] = line[2].substring(0, 1);
        }
        crfModel.tag(table);
        if (MyNLP.Config.DEBUG)
        {
            System.out.println(table);
        }
        CoNLLWord[] coNLLWordArray = new CoNLLWord[table.size()];
        for (int i = 0; i < coNLLWordArray.length; i++)
        {
            coNLLWordArray[i] = new CoNLLWord(i + 1, table.v[i][0], table.v[i][2], table.v[i][1]);
        }
        int i = 0;
        for (String[] line : table.v)
        {
            CRFModelForDependency.DTag dTag = new CRFModelForDependency.DTag(line[3]);
            if (dTag.pos.endsWith("ROOT"))
            {
                coNLLWordArray[i].HEAD = CoNLLWord.ROOT;
            }
            else
            {
                int index = convertOffset2Index(dTag, table, i);
                if (index == -1)
                    coNLLWordArray[i].HEAD = CoNLLWord.NULL;
                else coNLLWordArray[i].HEAD = coNLLWordArray[index];
            }
            ++i;
        }

        for (i = 0; i < coNLLWordArray.length; i++)
        {
            coNLLWordArray[i].DEPREL = BigramDependencyModel.get(coNLLWordArray[i].NAME, coNLLWordArray[i].POSTAG, coNLLWordArray[i].HEAD.NAME, coNLLWordArray[i].HEAD.POSTAG);
        }
        return new CoNLLSentence(coNLLWordArray);
    }

    static int convertOffset2Index(CRFModelForDependency.DTag dTag, Table table, int current)
    {
        int posCount = 0;
        if (dTag.offset > 0)
        {
            for (int i = current + 1; i < table.size(); ++i)
            {
                if (table.v[i][1].equals(dTag.pos)) ++posCount;
                if (posCount == dTag.offset) return i;
            }
        }
        else
        {
            for (int i = current - 1; i >= 0; --i)
            {
                if (table.v[i][1].equals(dTag.pos)) ++posCount;
                if (posCount == -dTag.offset) return i;
            }
        }

        return -1;
    }

    
    static class CRFModelForDependency extends CRFModel
    {

        public CRFModelForDependency(ITrie<FeatureFunction> featureFunctionTrie)
        {
            super(featureFunctionTrie);
        }

        
        static class DTag
        {
            int offset;
            String pos;

            public DTag(String tag)
            {
                String[] args = tag.split("_", 2);
                if (args[0].charAt(0) == '+') args[0] = args[0].substring(1);
                offset = Integer.parseInt(args[0]);
                pos = args[1];
            }

            @Override
            public String toString()
            {
                return (offset > 0 ? "+" : "") + offset + "_" + pos;
            }
        }

        DTag[] id2dtag;

        @Override
        public boolean load(ByteArray byteArray)
        {
            if (!super.load(byteArray)) return false;
            initId2dtagArray();
            return true;
        }

        private void initId2dtagArray()
        {
            id2dtag = new DTag[id2tag.length];
            for (int i = 0; i < id2tag.length; i++)
            {
                id2dtag[i] = new DTag(id2tag[i]);
            }
        }

        @Override
        protected void onLoadTxtFinished()
        {
            super.onLoadTxtFinished();
            initId2dtagArray();
        }

        boolean isLegal(int tagId, int current, Table table)
        {
            DTag tag = id2dtag[tagId];
            if ("ROOT".equals(tag.pos))
            {
                for (int i = 0; i < current; ++i)
                {
                    if (table.v[i][3].endsWith("ROOT")) return false;
                }
                return true;
            }
            else
            {
                int posCount = 0;
                if (tag.offset > 0)
                {
                    for (int i = current + 1; i < table.size(); ++i)
                    {
                        if (table.v[i][1].equals(tag.pos)) ++posCount;
                        if (posCount == tag.offset) return true;
                    }
                    return false;
                }
                else
                {
                    for (int i = current - 1; i >= 0; --i)
                    {
                        if (table.v[i][1].equals(tag.pos)) ++posCount;
                        if (posCount == -tag.offset) return true;
                    }
                    return false;
                }
            }
        }

        @Override
        public void tag(Table table)
        {
            int size = table.size();
            double bestScore = Double.MIN_VALUE;
            int bestTag = 0;
            int tagSize = id2tag.length;
            LinkedList<double[]> scoreList = computeScoreList(table, 0);    // 0位置命中的特征函数
            for (int i = 0; i < tagSize; ++i)   // -1位置的标签遍历
            {
                for (int j = 0; j < tagSize; ++j)   // 0位置的标签遍历
                {
                    if (!isLegal(j, 0, table)) continue;
                    double curScore = computeScore(scoreList, j);
                    if (matrix != null)
                    {
                        curScore += matrix[i][j];
                    }
                    if (curScore > bestScore)
                    {
                        bestScore = curScore;
                        bestTag = j;
                    }
                }
            }
            table.setLast(0, id2tag[bestTag]);
            int preTag = bestTag;
            // 0位置打分完毕，接下来打剩下的
            for (int i = 1; i < size; ++i)
            {
                scoreList = computeScoreList(table, i);    // i位置命中的特征函数
                bestScore = Double.MIN_VALUE;
                for (int j = 0; j < tagSize; ++j)   // i位置的标签遍历
                {
                    if (!isLegal(j, i, table)) continue;
                    double curScore =  computeScore(scoreList, j);
                    if (matrix != null)
                    {
                        curScore += matrix[preTag][j];
                    }
                    if (curScore > bestScore)
                    {
                        bestScore = curScore;
                        bestTag = j;
                    }
                }
                table.setLast(i, id2tag[bestTag]);
                preTag = bestTag;
            }
        }
    }
}
