
package in.thyferny.nlp.seg.common;


public class ResultTerm<V>
{
    public String word;
    public V label;
    public int offset;

    public ResultTerm(String word, V label, int offset)
    {
        this.word = word;
        this.label = label;
        this.offset = offset;
    }

    @Override
    public String toString()
    {
        return word + '/' + label;
    }
}
