
package in.thyferny.nlp.dictionary.py;

import java.util.*;

import in.thyferny.nlp.algoritm.ahocorasick.trie.Token;
import in.thyferny.nlp.algoritm.ahocorasick.trie.Trie;
import in.thyferny.nlp.corpus.dictionary.StringDictionary;


public class TonePinyinString2PinyinConverter
{
    
    static Map<String, Pinyin> mapKey;
    
    static Map<String, Pinyin> mapNumberKey;
    static Trie trie;
    static
    {
        mapNumberKey = new TreeMap<String, Pinyin>();
        mapKey = new TreeMap<String, Pinyin>();
        for (Pinyin pinyin : Integer2PinyinConverter.pinyins)
        {
            mapNumberKey.put(pinyin.toString(), pinyin);
            String pinyinWithToneMark = pinyin.getPinyinWithToneMark();
            String pinyinWithoutTone = pinyin.getPinyinWithoutTone();
            Pinyin tone5 = String2PinyinConverter.convert2Tone5(pinyin);
            mapKey.put(pinyinWithToneMark, pinyin);
            mapKey.put(pinyinWithoutTone, tone5);
        }
        trie = new Trie().remainLongest();
        trie.addAllKeyword(mapKey.keySet());
    }

    
    public static boolean valid(String singlePinyin)
    {
        if (mapNumberKey.containsKey(singlePinyin)) return true;

        return false;
    }

    public static Pinyin convertFromToneNumber(String singlePinyin)
    {
        return mapNumberKey.get(singlePinyin);
    }

    public static List<Pinyin> convert(String[] pinyinArray)
    {
        List<Pinyin> pinyinList = new ArrayList<Pinyin>(pinyinArray.length);
        for (int i = 0; i < pinyinArray.length; i++)
        {
            pinyinList.add(mapKey.get(pinyinArray[i]));
        }

        return pinyinList;
    }

    public static Pinyin convert(String singlePinyin)
    {
        return mapKey.get(singlePinyin);
    }

    
    public static List<Pinyin> convert(String tonePinyinText, boolean removeNull)
    {
        List<Pinyin> pinyinList = new LinkedList<Pinyin>();
        Collection<Token> tokenize = trie.tokenize(tonePinyinText);
        for (Token token : tokenize)
        {
            Pinyin pinyin = mapKey.get(token.getFragment());
            if (removeNull && pinyin == null) continue;
            pinyinList.add(pinyin);
        }

        return pinyinList;
    }

    
    public static boolean valid(String[] pinyinStringArray)
    {
        for (String p : pinyinStringArray)
        {
            if (!valid(p)) return false;
        }

        return true;
    }

    public static List<Pinyin> convertFromToneNumber(String[] pinyinArray)
    {
        List<Pinyin> pinyinList = new ArrayList<Pinyin>(pinyinArray.length);
        for (String py : pinyinArray)
        {
            pinyinList.add(convertFromToneNumber(py));
        }
        return pinyinList;
    }
}
