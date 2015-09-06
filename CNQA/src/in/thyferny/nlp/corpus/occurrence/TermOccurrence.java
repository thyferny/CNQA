
package in.thyferny.nlp.corpus.occurrence;

import java.util.List;

import in.thyferny.nlp.collection.trie.bintrie.BinTrie;


public class TermOccurrence
{
    
    BinTrie<TermFrequency> trieSingle;
    int totalTerm;

    public TermOccurrence()
    {
        trieSingle = new BinTrie<TermFrequency>();
    }

    public void add(String term)
    {
        TermFrequency value = trieSingle.get(term);
        if (value == null)
        {
            value = new TermFrequency(term);
            trieSingle.put(term, value);
        }
        else
        {
            value.increase();
        }
        ++totalTerm;
    }

    public void addAll(List<String> termList)
    {
        for (String s : termList)
        {
            add(s);
        }
    }

    public java.util.Set<java.util.Map.Entry<String, TermFrequency>> getEntrySet()
    {
        return trieSingle.entrySet();
    }
}
