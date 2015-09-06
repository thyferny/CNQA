
package in.thyferny.nlp.dictionary.other;
import static in.thyferny.nlp.utility.Predefine.logger;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.corpus.io.ByteArray;


public class CharType
{
    
    public static final byte CT_SINGLE = 5;

    
    public static final byte CT_DELIMITER = CT_SINGLE + 1;

    
    public static final byte CT_CHINESE = CT_SINGLE + 2;

    
    public static final byte CT_LETTER = CT_SINGLE + 3;

    
    public static final byte CT_NUM = CT_SINGLE + 4;

    
    public static final byte CT_INDEX = CT_SINGLE + 5;

    
    public static final byte CT_OTHER = CT_SINGLE + 12;
    
    static byte[] type;

    static
    {
        type = new byte[65536];
        logger.info("字符类型对应表开始加载 " + MyNLP.Config.CharTypePath);
        long start = System.currentTimeMillis();
        ByteArray byteArray = ByteArray.createByteArray(MyNLP.Config.CharTypePath);
        if (byteArray == null)
        {
            System.err.println("字符类型对应表加载失败：" + MyNLP.Config.CharTypePath);
            System.exit(-1);
        }
        else
        {
            while (byteArray.hasMore())
            {
                int b = byteArray.nextChar();
                int e = byteArray.nextChar();
                byte t = byteArray.nextByte();
                for (int i = b; i <= e; ++i)
                {
                    type[i] = t;
                }
            }
            logger.info("字符类型对应表加载成功，耗时" + (System.currentTimeMillis() - start) + " ms");
        }
    }

    
    public static byte get(char c)
    {
        return type[(int)c];
    }
}
