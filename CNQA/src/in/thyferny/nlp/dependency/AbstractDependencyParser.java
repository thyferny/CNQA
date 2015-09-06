
package in.thyferny.nlp.dependency;

import java.util.List;

import in.thyferny.nlp.corpus.dependency.CoNll.CoNLLSentence;
import in.thyferny.nlp.seg.common.Term;


public abstract class AbstractDependencyParser
{
    public abstract CoNLLSentence parse(List<Term> termList);
}
