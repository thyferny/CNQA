
package in.thyferny.nlp.tokenizer;

import java.util.List;

import in.thyferny.nlp.seg.Segment;
import in.thyferny.nlp.seg.Other.DoubleArrayTrieSegment;
import in.thyferny.nlp.seg.common.Term;


public class SpeedTokenizer
{
    
    public static final Segment SEGMENT = new DoubleArrayTrieSegment();
    public static List<Term> segment(String text)
    {
        return SEGMENT.seg(text.toCharArray());
    }

    
    public static List<Term> segment(char[] text)
    {
        return SEGMENT.seg(text);
    }

    
    public static List<List<Term>> seg2sentence(String text)
    {
        return SEGMENT.seg2sentence(text);
    }
}
