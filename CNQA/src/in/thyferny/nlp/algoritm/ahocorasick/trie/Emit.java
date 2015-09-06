package in.thyferny.nlp.algoritm.ahocorasick.trie;

import in.thyferny.nlp.algoritm.ahocorasick.interval.Interval;
import in.thyferny.nlp.algoritm.ahocorasick.interval.Intervalable;


public class Emit extends Interval implements Intervalable
{
    
    private final String keyword;

    
    public Emit(final int start, final int end, final String keyword)
    {
        super(start, end);
        this.keyword = keyword;
    }

    
    public String getKeyword()
    {
        return this.keyword;
    }

    @Override
    public String toString()
    {
        return super.toString() + "=" + this.keyword;
    }
}
