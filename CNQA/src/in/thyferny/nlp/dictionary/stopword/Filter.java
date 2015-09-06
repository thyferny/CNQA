
package in.thyferny.nlp.dictionary.stopword;

import in.thyferny.nlp.seg.common.Term;


public interface Filter
{
    
    boolean shouldInclude(Term term);
}
