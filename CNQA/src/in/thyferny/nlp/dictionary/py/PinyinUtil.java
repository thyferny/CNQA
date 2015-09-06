
package in.thyferny.nlp.dictionary.py;

import java.util.ArrayList;
import java.util.List;


public class PinyinUtil
{
    
    public static String convertToneNumber2ToneMark(final String pinyinStr)
    {
        String lowerCasePinyinStr = pinyinStr.toLowerCase();

        if (lowerCasePinyinStr.matches("[a-z]*[1-5]?"))
        {
            final char defautlCharValue = '$';
            final int defautlIndexValue = -1;

            char unmarkedVowel = defautlCharValue;
            int indexOfUnmarkedVowel = defautlIndexValue;

            final char charA = 'a';
            final char charE = 'e';
            final String ouStr = "ou";
            final String allUnmarkedVowelStr = "aeiouv";
            final String allMarkedVowelStr = "āáǎàaēéěèeīíǐìiōóǒòoūúǔùuǖǘǚǜü";

            if (lowerCasePinyinStr.matches("[a-z]*[1-5]"))
            {

                int tuneNumber =
                        Character.getNumericValue(lowerCasePinyinStr.charAt(lowerCasePinyinStr.length() - 1));

                int indexOfA = lowerCasePinyinStr.indexOf(charA);
                int indexOfE = lowerCasePinyinStr.indexOf(charE);
                int ouIndex = lowerCasePinyinStr.indexOf(ouStr);

                if (-1 != indexOfA)
                {
                    indexOfUnmarkedVowel = indexOfA;
                    unmarkedVowel = charA;
                }
                else if (-1 != indexOfE)
                {
                    indexOfUnmarkedVowel = indexOfE;
                    unmarkedVowel = charE;
                }
                else if (-1 != ouIndex)
                {
                    indexOfUnmarkedVowel = ouIndex;
                    unmarkedVowel = ouStr.charAt(0);
                }
                else
                {
                    for (int i = lowerCasePinyinStr.length() - 1; i >= 0; i--)
                    {
                        if (String.valueOf(lowerCasePinyinStr.charAt(i)).matches(
                                "[" + allUnmarkedVowelStr + "]"))
                        {
                            indexOfUnmarkedVowel = i;
                            unmarkedVowel = lowerCasePinyinStr.charAt(i);
                            break;
                        }
                    }
                }

                if ((defautlCharValue != unmarkedVowel) && (defautlIndexValue != indexOfUnmarkedVowel))
                {
                    int rowIndex = allUnmarkedVowelStr.indexOf(unmarkedVowel);
                    int columnIndex = tuneNumber - 1;

                    int vowelLocation = rowIndex * 5 + columnIndex;

                    char markedVowel = allMarkedVowelStr.charAt(vowelLocation);

                    StringBuffer resultBuffer = new StringBuffer();

                    resultBuffer.append(lowerCasePinyinStr.substring(0, indexOfUnmarkedVowel).replaceAll("v",
                                                                                                         "ü"));
                    resultBuffer.append(markedVowel);
                    resultBuffer.append(lowerCasePinyinStr.substring(indexOfUnmarkedVowel + 1,
                                                                     lowerCasePinyinStr.length() - 1).replaceAll("v", "ü"));

                    return resultBuffer.toString();

                }
                else
                // error happens in the procedure of locating vowel
                {
                    return lowerCasePinyinStr;
                }
            }
            else
            // input string has no any tune number
            {
                // only replace v with ü (umlat) character
                return lowerCasePinyinStr.replaceAll("v", "ü");
            }
        }
        else
        // bad format
        {
            return lowerCasePinyinStr;
        }
    }

    
    public static Pinyin[] convertList2Array(List<Pinyin> pinyinList)
    {
        return pinyinList.toArray(new Pinyin[0]);
    }

    public static Pinyin removeTone(Pinyin p)
    {
        return Pinyin.none5;
    }

    
    public static List<String> convertPinyinList2TonePinyinList(List<Pinyin> pinyinList)
    {
        List<String> tonePinyinList = new ArrayList<String>(pinyinList.size());
        for (Pinyin pinyin : pinyinList)
        {
            tonePinyinList.add(pinyin.getPinyinWithToneMark());
        }

        return tonePinyinList;
    }
}
