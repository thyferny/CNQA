
package in.thyferny.nlp.dictionary;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.algoritm.EditDistance;
import in.thyferny.nlp.dictionary.common.CommonSynonymDictionary;
import in.thyferny.nlp.seg.common.Term;

public class CoreSynonymDictionary
{
    static CommonSynonymDictionary dictionary;

    static
    {
        try
        {
            long start = System.currentTimeMillis();
            dictionary = CommonSynonymDictionary.create(new FileInputStream(MyNLP.Config.CoreSynonymDictionaryDictionaryPath));
            logger.info("载入核心同义词词典成功，耗时 " + (System.currentTimeMillis() - start) + " ms");
        }
        catch (Exception e)
        {
            System.err.println("载入核心同义词词典失败" + e);
            System.exit(-1);
        }
    }

    public static CommonSynonymDictionary.SynonymItem get(String key)
    {
        return dictionary.get(key);
    }

    
    public static long distance(CommonSynonymDictionary.SynonymItem itemA, CommonSynonymDictionary.SynonymItem itemB)
    {
        return itemA.distance(itemB);
    }

    
    public static long distance(String A, String B)
    {
        CommonSynonymDictionary.SynonymItem itemA = get(A);
        CommonSynonymDictionary.SynonymItem itemB = get(B);
        if (itemA == null || itemB == null) return Long.MAX_VALUE;

        return distance(itemA, itemB);
    }

    
    public static double similarity(String A, String B)
    {
        long distance = distance(A, B);
        if (distance > dictionary.getMaxSynonymItemIdDistance()) return 0.0;

        return (dictionary.getMaxSynonymItemIdDistance() - distance) / (double) dictionary.getMaxSynonymItemIdDistance();
    }

    
    public static List<CommonSynonymDictionary.SynonymItem> convert(List<Term> sentence, boolean withUndefinedItem)
    {
        List<CommonSynonymDictionary.SynonymItem> synonymItemList = new ArrayList<CommonSynonymDictionary.SynonymItem>(sentence.size());
        for (Term term : sentence)
        {
            CommonSynonymDictionary.SynonymItem item = get(term.word);
            if (item == null)
            {
                if (withUndefinedItem)
                {
                    item = CommonSynonymDictionary.SynonymItem.createUndefined(term.word);
                    synonymItemList.add(item);
                }

            }
            else
            {
                synonymItemList.add(item);
            }
        }

        return synonymItemList;
    }

    
    public static long[] getLexemeArray(List<CommonSynonymDictionary.SynonymItem> synonymItemList)
    {
        long[] array = new long[synonymItemList.size()];
        int i = 0;
        for (CommonSynonymDictionary.SynonymItem item : synonymItemList)
        {
            array[i++] = item.entry.id;
        }
        return array;
    }


    public long distance(List<CommonSynonymDictionary.SynonymItem> synonymItemListA, List<CommonSynonymDictionary.SynonymItem> synonymItemListB)
    {
        return EditDistance.compute(synonymItemListA, synonymItemListB);
    }

    public long distance(long[] arrayA, long[] arrayB)
    {
        return EditDistance.compute(arrayA, arrayB);
    }
}
