
package in.thyferny.nlp.dictionary.other;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import in.thyferny.nlp.MyNLP;


public class CharTable
{
    
    public static char[] CONVERT;

    static
    {
        long start = System.currentTimeMillis();
        try
        {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(MyNLP.Config.CharTablePath));
            CONVERT = (char[]) in.readObject();
            in.close();
        }
        catch (Exception e)
        {
            logger.severe("字符正规化表加载失败，原因如下：");
            e.printStackTrace();
            System.exit(-1);
        }

        logger.info("字符正规化表加载成功：" + (System.currentTimeMillis() - start) + " ms");
    }

    
    public static char convert(char c)
    {
        return CONVERT[c];
    }

    public static char[] convert(char[] charArray)
    {
        char[] result = new char[charArray.length];
        for (int i = 0; i < charArray.length; i++)
        {
            result[i] = CONVERT[charArray[i]];
        }

        return result;
    }

    public static String convert(String charArray)
    {
        assert charArray != null;
        char[] result = new char[charArray.length()];
        for (int i = 0; i < charArray.length(); i++)
        {
            result[i] = CONVERT[charArray.charAt(i)];
        }

        return new String(result);
    }

    
    public static void normalization(char[] charArray)
    {
        assert charArray != null;
        for (int i = 0; i < charArray.length; i++)
        {
            charArray[i] = CONVERT[charArray[i]];
        }
    }
}
