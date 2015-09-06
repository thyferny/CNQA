
package in.thyferny.nlp.dictionary.nr;

import in.thyferny.nlp.dictionary.CoreDictionary;
import in.thyferny.nlp.utility.Predefine;


public class NRConstant
{
    
    public static final int WORD_ID = CoreDictionary.getWordID(Predefine.TAG_PEOPLE);
    
    public static final CoreDictionary.Attribute ATTRIBUTE = CoreDictionary.get(WORD_ID);
}
