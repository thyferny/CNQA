
package in.thyferny.nlp.corpus.dictionary;

import java.util.LinkedList;
import java.util.List;


public class StringDictionaryMaker
{
    
    public static StringDictionary load(String path, String separator)
    {
        StringDictionary dictionary = new StringDictionary(separator);
        if (dictionary.load(path)) return dictionary;
        return null;
    }

    
    public static StringDictionary load(String path)
    {
        return load(path, "=");
    }

    
    public static StringDictionary combine(StringDictionary... args)
    {
        StringDictionary[] dictionaries = args.clone();
        StringDictionary mainDictionary = dictionaries[0];
        for (int i = 1; i < dictionaries.length; ++i)
        {
            mainDictionary.combine(dictionaries[i]);
        }

        return mainDictionary;
    }

    public static StringDictionary combine(String... args)
    {
        String[] pathArray = args.clone();
        List<StringDictionary> dictionaryList = new LinkedList<StringDictionary>();
        for (String path : pathArray)
        {
            StringDictionary dictionary = load(path);
            if (dictionary == null) continue;
            dictionaryList.add(dictionary);
        }

        return combine(dictionaryList.toArray(new StringDictionary[0]));
    }
}
