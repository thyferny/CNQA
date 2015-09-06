
package in.thyferny.nlp.corpus.dictionary;

import java.util.*;

import in.thyferny.nlp.collection.trie.bintrie.BinTrie;


public class SuffixDictionary
{
    BinTrie<Integer> trie;

    public SuffixDictionary()
    {
        trie = new BinTrie<Integer>();
    }

    
    public void add(String word)
    {
        word = reverse(word);
        trie.put(word, word.length());
    }

    public void addAll(String total)
    {
        for (int i = 0; i < total.length(); ++i)
        {
            add(String.valueOf(total.charAt(i)));
        }
    }

    public void addAll(String[] total)
    {
        for (String single : total)
        {
            add(single);
        }
    }

    
    public int get(String suffix)
    {
        suffix = reverse(suffix);
        Integer length = trie.get(suffix);
        if (length == null) return 0;

        return length;
    }

    
    public boolean endsWith(String word)
    {
        word = reverse(word);
        return trie.commonPrefixSearchWithValue(word).size() > 0;
    }

    
    public int getLongestSuffixLength(String word)
    {
        word = reverse(word);
        LinkedList<Map.Entry<String, Integer>> suffixList = trie.commonPrefixSearchWithValue(word);
        if (suffixList.size() == 0) return 0;
        return suffixList.getLast().getValue();
    }

    private static String reverse(String word)
    {
        return new StringBuilder(word).reverse().toString();
    }

    
    public Set<Map.Entry<String, Integer>> entrySet()
    {
        Set<Map.Entry<String, Integer>> treeSet = new LinkedHashSet<Map.Entry<String, Integer>>();
        for (Map.Entry<String, Integer> entry : trie.entrySet())
        {
            treeSet.add(new AbstractMap.SimpleEntry<String, Integer>(reverse(entry.getKey()), entry.getValue()));
        }

        return treeSet;
    }
}
