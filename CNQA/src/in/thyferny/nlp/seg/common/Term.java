
package in.thyferny.nlp.seg.common;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.corpus.tag.Nature;


public class Term
{
    
    public String word;

    
    public Nature nature;

    
    public int offset;

    
    public Term(String word, Nature nature)
    {
        this.word = word;
        this.nature = nature;
    }

    @Override
    public String toString()
    {
        if (MyNLP.Config.ShowTermNature)
            return word + "/" + nature;
        return word;
    }

    
    public int length()
    {
        return word.length();
    }
}
