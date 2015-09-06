
package in.thyferny.nlp.corpus.document.sentence.word;


public class WordFactory
{
    
    public static IWord create(String param)
    {
        if (param == null) return null;
        if (param.startsWith("[") && !param.startsWith("[/"))
        {
            return CompoundWord.create(param);
        }
        else
        {
            return Word.create(param);
        }
    }
}
