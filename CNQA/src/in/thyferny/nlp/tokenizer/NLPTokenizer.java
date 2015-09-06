
package in.thyferny.nlp.tokenizer;

import java.util.List;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.seg.Segment;
import in.thyferny.nlp.seg.Dijkstra.DijkstraSegment;
import in.thyferny.nlp.seg.common.Term;


public class NLPTokenizer
{
    
    public static final Segment SEGMENT = MyNLP.newSegment().enableNameRecognize(true).enableTranslatedNameRecognize(true)
            .enableJapaneseNameRecognize(true).enablePlaceRecognize(true).enableOrganizationRecognize(true)
            .enablePartOfSpeechTagging(true);

    public static List<Term> segment(String text)
    {
        return SEGMENT.seg(text);
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
