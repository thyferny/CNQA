
package in.thyferny.nlp.corpus.dictionary;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.util.List;

import in.thyferny.nlp.corpus.document.sentence.word.IWord;
import in.thyferny.nlp.corpus.document.sentence.word.Word;

public abstract class CommonDictionaryMaker implements ISaveAble
{
    static boolean verbose = false;
    
    EasyDictionary dictionary;
    
    DictionaryMaker dictionaryMaker;
    
    NGramDictionaryMaker nGramDictionaryMaker;

    public CommonDictionaryMaker(EasyDictionary dictionary)
    {
        nGramDictionaryMaker = new NGramDictionaryMaker();
        dictionaryMaker = new DictionaryMaker();
        this.dictionary = dictionary;
    }

    @Override
    public boolean saveTxtTo(String path)
    {
        if (dictionaryMaker.saveTxtTo(path + ".txt"))
        {
            if (nGramDictionaryMaker.saveTxtTo(path))
            {
                return true;
            }
        }

        return false;
    }

    
    public void compute(List<List<IWord>> sentenceList)
    {
        roleTag(sentenceList);
        addToDictionary(sentenceList);
    }

    
    abstract protected void addToDictionary(List<List<IWord>> sentenceList);

    
    abstract protected void roleTag(List<List<IWord>> sentenceList);
}
