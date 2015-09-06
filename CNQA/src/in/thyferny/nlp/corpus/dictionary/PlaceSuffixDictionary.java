
package in.thyferny.nlp.corpus.dictionary;

import in.thyferny.nlp.corpus.dictionary.SuffixDictionary;
import in.thyferny.nlp.utility.Predefine;


public class PlaceSuffixDictionary
{
    public static SuffixDictionary dictionary = new SuffixDictionary();
    static
    {
        dictionary.addAll(Predefine.POSTFIX_SINGLE);
        dictionary.addAll(Predefine.POSTFIX_MUTIPLE);
    }
}
