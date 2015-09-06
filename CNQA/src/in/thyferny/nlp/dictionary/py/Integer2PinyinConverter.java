
package in.thyferny.nlp.dictionary.py;


public class Integer2PinyinConverter
{
    public static final Pinyin[] pinyins = Pinyin.values();

    public static Pinyin getPinyin(int ordinal)
    {
        return pinyins[ordinal];
    }
}
