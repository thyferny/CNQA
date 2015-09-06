
package in.thyferny.nlp.dictionary.ts;

import static in.thyferny.nlp.utility.Predefine.logger;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import in.thyferny.nlp.collection.trie.DoubleArrayTrie;
import in.thyferny.nlp.utility.Predefine;


public class SimplifiedChineseDictionary extends BaseChineseDictionary
{
    
    static AhoCorasickDoubleArrayTrie<String> trie = new AhoCorasickDoubleArrayTrie<String>();
    
    static
    {
        long start = System.currentTimeMillis();
        if (!load(MyNLP.Config.TraditionalChineseDictionaryPath, trie, true))
        {
            throw new IllegalArgumentException("简繁词典" + MyNLP.Config.TraditionalChineseDictionaryPath + Predefine.REVERSE_EXT + "加载失败");
        }

        logger.info("简繁词典" + MyNLP.Config.TraditionalChineseDictionaryPath + Predefine.REVERSE_EXT + "加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
    }

    public static String convertToTraditionalChinese(String simplifiedChineseString)
    {
        return segLongest(simplifiedChineseString.toCharArray(), trie);
    }

    public static String convertToTraditionalChinese(char[] simplifiedChinese)
    {
        return segLongest(simplifiedChinese, trie);
    }
}
