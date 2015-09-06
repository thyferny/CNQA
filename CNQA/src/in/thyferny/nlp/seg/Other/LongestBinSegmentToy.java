
package in.thyferny.nlp.seg.Other;


import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import in.thyferny.nlp.collection.trie.bintrie.BaseNode;
import in.thyferny.nlp.collection.trie.bintrie.BinTrie;


public class LongestBinSegmentToy<V>
{
    private BinTrie<V> trie;
    
    private char c[];
    
    private int offset;

    public LongestBinSegmentToy(BinTrie<V> trie)
    {
        this.trie = trie;
    }

    public List<Map.Entry<String, V>> seg(String text)
    {
        reset(text);
        List<Map.Entry<String, V>> allWords = new ArrayList<Map.Entry<String, V>>();
        Map.Entry<String, V> entry;
        while ((entry = next()) != null)
        {
            allWords.add(entry);
        }
        c = null;
        return allWords;
    }

    
    public void reset(String text)
    {
        offset = 0;
        c = text.toCharArray();
    }

    public Map.Entry<String, V> next()
    {
        StringBuffer key = new StringBuffer();  // 构造key
        BaseNode branch = trie;
        BaseNode possibleBranch = null;
        while (offset < c.length)
        {
            if (possibleBranch != null)
            {
                branch = possibleBranch;
                possibleBranch = null;
            }
            else
            {
                branch = branch.getChild(c[offset]);
                if (branch == null)
                {
                    branch = trie;
                    ++offset;
                    continue;
                }
            }
            key.append(c[offset]);
            ++offset;
            if (branch.getStatus() == BaseNode.Status.WORD_END_3
//                    || branch.getStatus() == BaseNode.Status.WORD_MIDDLE_2
                    )
            {
                return new AbstractMap.SimpleEntry<String, V>(key.toString(), (V) branch.getValue());
            }
            else if (branch.getStatus() == BaseNode.Status.WORD_MIDDLE_2)   // 最长分词的关键
            {
                possibleBranch = offset < c.length ? branch.getChild(c[offset]) : null;
                if (possibleBranch == null)
                {
                    return new AbstractMap.SimpleEntry<String, V>(key.toString(), (V) branch.getValue());
                }
            }
        }

        return null;
    }

    
    public int getOffset()
    {
        return offset;
    }
}
