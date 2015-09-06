
package in.thyferny.nlp.seg.common.wrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import in.thyferny.nlp.seg.Segment;
import in.thyferny.nlp.seg.common.Term;


public class SegmentWrapper
{
    BufferedReader br;
    Segment segment;
    
    Term[] termArray;
    
    int index;

    public SegmentWrapper(BufferedReader br, Segment segment)
    {
        this.br = br;
        this.segment = segment;
    }

    
    public void reset(BufferedReader br)
    {
        this.br = br;
        termArray = null;
        index = 0;
    }

    public Term next() throws IOException
    {
        if (termArray != null && index < termArray.length) return termArray[index++];
        String line = br.readLine();
        if (line == null) return null;
        List<Term> termList = segment.seg(line);
        if (termList.size() == 0) return null;
        termArray = termList.toArray(new Term[0]);
        index = 0;

        return termArray[index++];
    }
}
