
package in.thyferny.nlp.phrase;

import java.util.List;


public interface IPhraseExtractor
{
    
    List<String> extractPhrase(String text, int size);
}
