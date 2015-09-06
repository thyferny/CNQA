
package in.thyferny.nlp.dictionary.ns;

import static in.thyferny.nlp.corpus.tag.NR.B;
import static in.thyferny.nlp.utility.Predefine.logger;

import java.util.*;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.algoritm.ahocorasick.trie.Emit;
import in.thyferny.nlp.algoritm.ahocorasick.trie.Trie;
import in.thyferny.nlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import in.thyferny.nlp.corpus.dictionary.item.EnumItem;
import in.thyferny.nlp.corpus.tag.NR;
import in.thyferny.nlp.corpus.tag.NS;
import in.thyferny.nlp.dictionary.CoreDictionary;
import in.thyferny.nlp.dictionary.TransformMatrixDictionary;
import in.thyferny.nlp.dictionary.nr.NRDictionary;
import in.thyferny.nlp.seg.common.Vertex;
import in.thyferny.nlp.seg.common.WordNet;
import in.thyferny.nlp.utility.Predefine;


public class PlaceDictionary
{
    
    public static NSDictionary dictionary;
    
    public static TransformMatrixDictionary<NS> transformMatrixDictionary;
    
    public static AhoCorasickDoubleArrayTrie<String> trie;

    
    static final int WORD_ID = CoreDictionary.getWordID(Predefine.TAG_PLACE);
    
    static final CoreDictionary.Attribute ATTRIBUTE = CoreDictionary.get(WORD_ID);

    static
    {
        long start = System.currentTimeMillis();
        dictionary = new NSDictionary();
        dictionary.load(MyNLP.Config.PlaceDictionaryPath);
        logger.info(MyNLP.Config.PlaceDictionaryPath + "加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
        transformMatrixDictionary = new TransformMatrixDictionary<NS>(NS.class);
        transformMatrixDictionary.load(MyNLP.Config.PlaceDictionaryTrPath);
        trie = new AhoCorasickDoubleArrayTrie<String>();
        TreeMap<String, String> patternMap = new TreeMap<String, String>();
        patternMap.put("CH", "CH");
        patternMap.put("CDH", "CDH");
        patternMap.put("CDEH", "CDEH");
        patternMap.put("GH", "GH");
        trie.build(patternMap);
    }

    
    public static void parsePattern(List<NS> nsList, List<Vertex> vertexList, final WordNet wordNetOptimum, final WordNet wordNetAll)
    {
//        ListIterator<Vertex> listIterator = vertexList.listIterator();
        StringBuilder sbPattern = new StringBuilder(nsList.size());
        for (NS ns : nsList)
        {
            sbPattern.append(ns.toString());
        }
        String pattern = sbPattern.toString();
        final Vertex[] wordArray = vertexList.toArray(new Vertex[0]);
        trie.parseText(pattern, new AhoCorasickDoubleArrayTrie.IHit<String>()
        {
            @Override
            public void hit(int begin, int end, String value)
            {
                StringBuilder sbName = new StringBuilder();
                for (int i = begin; i < end; ++i)
                {
                    sbName.append(wordArray[i].realWord);
                }
                String name = sbName.toString();
                // 对一些bad case做出调整
                if (isBadCase(name)) return;

                // 正式算它是一个名字
                if (MyNLP.Config.DEBUG)
                {
                    System.out.printf("识别出地名：%s %s\n", name, value);
                }
                int offset = 0;
                for (int i = 0; i < begin; ++i)
                {
                    offset += wordArray[i].realWord.length();
                }
                wordNetOptimum.insert(offset, new Vertex(Predefine.TAG_PLACE, name, ATTRIBUTE, WORD_ID), wordNetAll);
            }
        });
    }

    
    static boolean isBadCase(String name)
    {
        EnumItem<NS> nrEnumItem = dictionary.get(name);
        if (nrEnumItem == null) return false;
        return nrEnumItem.containsLabel(NS.Z);
    }
}
