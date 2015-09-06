
package in.thyferny.nlp.corpus.occurrence;


public class PairFrequency extends TermFrequency
{
    
    public double mi;
    
    public double le;
    
    public double re;
    
    public double score;
    public String first;
    public String second;
    public char delimiter;

    protected PairFrequency(String term, Integer frequency)
    {
        super(term, frequency);
    }

    protected PairFrequency(String term)
    {
        super(term);
    }

    
    public static PairFrequency create(String first, char delimiter ,String second)
    {
        PairFrequency pairFrequency = new PairFrequency(first + delimiter + second);
        pairFrequency.first = first;
        pairFrequency.delimiter = delimiter;
        pairFrequency.second = second;
        return pairFrequency;
    }

    public boolean isRight()
    {
        return delimiter == Occurrence.RIGHT;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(first);
        sb.append(isRight() ? '→' : '←');
        sb.append(second);
        sb.append('=');
        sb.append(" tf=");
        sb.append(getValue());
        sb.append(' ');
        sb.append("mi=");
        sb.append(mi);
        sb.append(" le=");
        sb.append(le);
        sb.append(" re=");
        sb.append(re);
        sb.append(" score=");
        sb.append(score);
        return sb.toString();
    }
}
