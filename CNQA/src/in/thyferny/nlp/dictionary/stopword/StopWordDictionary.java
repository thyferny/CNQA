
package in.thyferny.nlp.dictionary.stopword;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.*;
import java.util.Collection;

import in.thyferny.nlp.collection.MDAG.MDAGSet;
import in.thyferny.nlp.dictionary.common.CommonDictionary;
import in.thyferny.nlp.seg.common.Term;


public class StopWordDictionary extends MDAGSet implements Filter
{
    public StopWordDictionary(File file) throws IOException
    {
        super(file);
    }

    public StopWordDictionary(Collection<String> strCollection)
    {
        super(strCollection);
    }

    public StopWordDictionary()
    {
    }

    @Override
    public boolean shouldInclude(Term term)
    {
        return contains(term.word);
    }
}
