
package in.thyferny.nlp.dictionary;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.*;
import java.util.*;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.collection.trie.DoubleArrayTrie;
import in.thyferny.nlp.corpus.io.ByteArray;
import in.thyferny.nlp.corpus.tag.Nature;
import in.thyferny.nlp.utility.Predefine;
import in.thyferny.nlp.utility.TextUtility;


public class CoreDictionary
{
    public static DoubleArrayTrie<Attribute> trie = new DoubleArrayTrie<Attribute>();
    public final static String path = MyNLP.Config.CoreDictionaryPath;
    public static final int totalFrequency = 221894;

    // 自动加载词典
    static
    {
        long start = System.currentTimeMillis();
        if (!load(path))
        {
            System.err.printf("核心词典%s加载失败\n", path);
            System.exit(-1);
        }
        else
        {
            logger.info(path + "加载成功，" + trie.size() + "个词条，耗时" + (System.currentTimeMillis() - start) + "ms");
        }
    }

    // 一些特殊的WORD_ID
    public static final int NR_WORD_ID = getWordID(Predefine.TAG_PEOPLE);
    public static final int NS_WORD_ID = getWordID(Predefine.TAG_PLACE);
    public static final int NT_WORD_ID = getWordID(Predefine.TAG_GROUP);
    public static final int T_WORD_ID = getWordID(Predefine.TAG_TIME);
    public static final int X_WORD_ID = getWordID(Predefine.TAG_CLUSTER);
    public static final int M_WORD_ID = getWordID(Predefine.TAG_NUMBER);
    public static final int NX_WORD_ID = getWordID(Predefine.TAG_PROPER);

    private static boolean load(String path)
    {
        logger.info("核心词典开始加载:" + path);
        if (loadDat(path)) return true;
        TreeMap<String, CoreDictionary.Attribute> map = new TreeMap<String, Attribute>();
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            String line;
            int MAX_FREQUENCY = 0;
            long start = System.currentTimeMillis();
            while ((line = br.readLine()) != null)
            {
                String param[] = line.split("\\s");
                int natureCount = (param.length - 1) / 2;
                CoreDictionary.Attribute attribute = new CoreDictionary.Attribute(natureCount);
                for (int i = 0; i < natureCount; ++i)
                {
                    attribute.nature[i] = Enum.valueOf(Nature.class, param[1 + 2 * i]);
                    attribute.frequency[i] = Integer.parseInt(param[2 + 2 * i]);
                    attribute.totalFrequency += attribute.frequency[i];
                }
                map.put(param[0], attribute);
                MAX_FREQUENCY += attribute.totalFrequency;
            }
            logger.info("核心词典读入词条" + map.size() + " 全部频次" + MAX_FREQUENCY + "，耗时" + (System.currentTimeMillis() - start) + "ms");
            br.close();
            trie.build(map);
            logger.info("核心词典加载成功:" + trie.size() + "个词条，下面将写入缓存……");
            try
            {
                DataOutputStream out = new DataOutputStream(new FileOutputStream(path + Predefine.BIN_EXT));
                Collection<CoreDictionary.Attribute> attributeList = map.values();
                out.writeInt(attributeList.size());
                for (CoreDictionary.Attribute attribute : attributeList)
                {
                    out.writeInt(attribute.totalFrequency);
                    out.writeInt(attribute.nature.length);
                    for (int i = 0; i < attribute.nature.length; ++i)
                    {
                        out.writeInt(attribute.nature[i].ordinal());
                        out.writeInt(attribute.frequency[i]);
                    }
                }
                trie.save(out);
                out.close();
            }
            catch (Exception e)
            {
                logger.warning("保存失败" + e);
                return false;
            }
        }
        catch (FileNotFoundException e)
        {
            logger.warning("核心词典" + path + "不存在！" + e);
            return false;
        }
        catch (IOException e)
        {
            logger.warning("核心词典" + path + "读取错误！" + e);
            return false;
        }

        return true;
    }

    
    static boolean loadDat(String path)
    {
        try
        {
            ByteArray byteArray = ByteArray.createByteArray(path + Predefine.BIN_EXT);
            if (byteArray == null) return false;
            int size = byteArray.nextInt();
            CoreDictionary.Attribute[] attributes = new CoreDictionary.Attribute[size];
            final Nature[] natureIndexArray = Nature.values();
            for (int i = 0; i < size; ++i)
            {
                // 第一个是全部频次，第二个是词性个数
                int currentTotalFrequency = byteArray.nextInt();
                int length = byteArray.nextInt();
                attributes[i] = new CoreDictionary.Attribute(length);
                attributes[i].totalFrequency = currentTotalFrequency;
                for (int j = 0; j < length; ++j)
                {
                    attributes[i].nature[j] = natureIndexArray[byteArray.nextInt()];
                    attributes[i].frequency[j] = byteArray.nextInt();
                }
            }
            if (!trie.load(byteArray, attributes) || byteArray.hasMore()) return false;
        }
        catch (Exception e)
        {
            logger.warning("读取失败，问题发生在" + e);
            return false;
        }
        return true;
    }

    
    public static Attribute get(String key)
    {
        return trie.get(key);
    }

    
    public static Attribute get(int wordID)
    {
        return trie.get(wordID);
    }

    
    public static int getTermFrequency(String term)
    {
        Attribute attribute = get(term);
        if (attribute == null) return 0;
        return attribute.totalFrequency;
    }

    
    public static boolean contains(String key)
    {
        return trie.get(key) != null;
    }

    
    static public class Attribute implements Serializable
    {
        
        public Nature nature[];
        
        public int frequency[];

        public int totalFrequency;

        // 几个预定义的变量

//        public static Attribute NUMBER = new Attribute()

        public Attribute(int size)
        {
            nature = new Nature[size];
            frequency = new int[size];
        }

        public Attribute(Nature[] nature, int[] frequency)
        {
            this.nature = nature;
            this.frequency = frequency;
        }

        public Attribute(Nature nature, int frequency)
        {
            this(1);
            this.nature[0] = nature;
            this.frequency[0] = frequency;
            totalFrequency = frequency;
        }

        public Attribute(Nature[] nature, int[] frequency, int totalFrequency)
        {
            this.nature = nature;
            this.frequency = frequency;
            this.totalFrequency = totalFrequency;
        }

        
        public Attribute(Nature nature)
        {
            this(nature, 1000);
        }

        public static Attribute create(String natureWithFrequency)
        {
            try
            {
                String param[] = natureWithFrequency.split(" ");
                int natureCount = param.length / 2;
                Attribute attribute = new Attribute(natureCount);
                for (int i = 0; i < natureCount; ++i)
                {
                    attribute.nature[i] = Enum.valueOf(Nature.class, param[2 * i]);
                    attribute.frequency[i] = Integer.parseInt(param[1 + 2 * i]);
                    attribute.totalFrequency += attribute.frequency[i];
                }
                return attribute;
            }
            catch (Exception e)
            {
                logger.warning("使用字符串" + natureWithFrequency + "创建词条属性失败！" + TextUtility.exceptionToString(e));
                return null;
            }
        }

        
        public int getNatureFrequency(String nature)
        {
            try
            {
                Nature pos = Enum.valueOf(Nature.class, nature);
                return getNatureFrequency(pos);
            }
            catch (IllegalArgumentException e)
            {
                return 0;
            }
        }

        
        public int getNatureFrequency(final Nature nature)
        {
            int result = 0;
            int i = 0;
            for (Nature pos : this.nature)
            {
                if (nature == pos)
                {
                    return frequency[i];
                }
                ++i;
            }
            return result;
        }

        
        public boolean hasNature(Nature nature)
        {
            return getNatureFrequency(nature) > 0;
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nature.length; ++i)
            {
                sb.append(nature[i]).append(' ').append(frequency[i]).append(' ');
            }
            return sb.toString();
        }
    }

    
    public static int getWordID(String a)
    {
        return CoreDictionary.trie.exactMatchSearch(a);
    }
}
