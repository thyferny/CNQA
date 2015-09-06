
package in.thyferny.nlp.dependency;

import java.util.List;

import in.thyferny.nlp.corpus.dependency.CoNll.CoNLLSentence;
import in.thyferny.nlp.dependency.common.Edge;
import in.thyferny.nlp.dependency.common.Node;
import in.thyferny.nlp.model.bigram.WordNatureDependencyModel;
import in.thyferny.nlp.seg.common.Term;
import in.thyferny.nlp.tokenizer.NLPTokenizer;


public class WordNatureDependencyParser extends MinimumSpanningTreeParser
{
    static final WordNatureDependencyParser INSTANCE = new WordNatureDependencyParser();

    public static CoNLLSentence compute(List<Term> termList)
    {
        return INSTANCE.parse(termList);
    }

    public static CoNLLSentence compute(String text)
    {
        return compute(NLPTokenizer.segment(text));
    }

    @Override
    protected Edge makeEdge(Node[] nodeArray, int from, int to)
    {
        return WordNatureDependencyModel.getEdge(nodeArray[from], nodeArray[to]);
    }
}
