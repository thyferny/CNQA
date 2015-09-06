
package in.thyferny.nlp.dictionary;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.corpus.io.ByteArray;
import in.thyferny.nlp.utility.Predefine;


public class CoreBiGramTableDictionary
{
    
    static int start[];
    
    static int pair[];

    public final static String path = MyNLP.Config.BiGramDictionaryPath;
    final static String datPath = MyNLP.Config.BiGramDictionaryPath + ".table" + Predefine.BIN_EXT;

    static
    {
        logger.info("开始加载二元词典" + path + ".table");
        long start = System.currentTimeMillis();
        if (!load(path))
        {
            logger.severe("二元词典加载失败");
            System.exit(-1);
        }
        else
        {
            logger.info(path + ".table" + "加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
        }
    }

    static boolean load(String path)
    {
        if (loadDat(datPath)) return true;
        BufferedReader br;
        TreeMap<Integer, TreeMap<Integer, Integer>> map = new TreeMap<Integer, TreeMap<Integer, Integer>>();
        try
        {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            String line;
            int total = 0;
            int maxWordId = CoreDictionary.trie.size();
            while ((line = br.readLine()) != null)
            {
                String[] params = line.split("\\s");
                String[] twoWord = params[0].split("@", 2);
                String a = twoWord[0];
                int idA = CoreDictionary.trie.exactMatchSearch(a);
                if (idA == -1)
                {
//                    if (HanLP.Config.DEBUG)
//                        logger.warning(line + " 中的 " + a + "不存在于核心词典，将会忽略这一行");
                    continue;
                }
                String b = twoWord[1];
                int idB = CoreDictionary.trie.exactMatchSearch(b);
                if (idB == -1)
                {
//                    if (HanLP.Config.DEBUG)
//                        logger.warning(line + " 中的 " + b + "不存在于核心词典，将会忽略这一行");
                    continue;
                }
                int freq = Integer.parseInt(params[1]);
                TreeMap<Integer, Integer> biMap = map.get(idA);
                if (biMap == null)
                {
                    biMap = new TreeMap<Integer, Integer>();
                    map.put(idA, biMap);
                }
                biMap.put(idB, freq);
                total += 2;
            }
            br.close();
            start = new int[maxWordId + 1];
            pair = new int[total];  // total是接续的个数*2
            int offset = 0;

            for (int i = 0; i < maxWordId; ++i)
            {
                TreeMap<Integer, Integer> bMap = map.get(i);
                if (bMap != null)
                {
                    for (Map.Entry<Integer, Integer> entry : bMap.entrySet())
                    {
                        int index = offset << 1;
                        pair[index] = entry.getKey();
                        pair[index + 1] = entry.getValue();
                        ++offset;
                    }
                }
                start[i + 1] = offset;
            }

            logger.info("二元词典读取完毕:" + path + "，构建为TableBin结构");
        }
        catch (FileNotFoundException e)
        {
            logger.severe("二元词典" + path + "不存在！" + e);
            return false;
        }
        catch (IOException e)
        {
            logger.severe("二元词典" + path + "读取错误！" + e);
            return false;
        }
        logger.info("开始缓存二元词典到" + datPath);
        if (!saveDat(datPath))
        {
            logger.warning("缓存二元词典到" + datPath + "失败");
        }
        return true;
    }

    static boolean saveDat(String path)
    {
        try
        {
//            DataOutputStream out = new DataOutputStream(new FileOutputStream(path));
//            out.writeInt(start.length);
//            for (int i : start)
//            {
//                out.writeInt(i);
//            }
//            out.writeInt(pair.length);
//            for (int i : pair)
//            {
//                out.writeInt(i);
//            }
//            out.close();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
            out.writeObject(start);
            out.writeObject(pair);
            out.close();
        }
        catch (Exception e)
        {
            logger.log(Level.WARNING, "在缓存" + path + "时发生异常", e);
            return false;
        }

        return true;
    }

    static boolean loadDat(String path)
    {
//        ByteArray byteArray = ByteArray.createByteArray(path);
//        if (byteArray == null) return false;
//
//        int size = byteArray.nextInt(); // 这两个数组从byte转为int竟然要花4秒钟
//        start = new int[size];
//        for (int i = 0; i < size; ++i)
//        {
//            start[i] = byteArray.nextInt();
//        }
//
//        size = byteArray.nextInt();
//        pair = new int[size];
//        for (int i = 0; i < size; ++i)
//        {
//            pair[i] = byteArray.nextInt();
//        }

        try
        {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
            start = (int[]) in.readObject();
            if (CoreDictionary.trie.size() != start.length - 1)     // 目前CoreNatureDictionary.ngram.txt的缓存依赖于CoreNatureDictionary.txt的缓存
            {                                                       // 所以这里校验一下二者的一致性，不然可能导致下标越界或者ngram错乱的情况
                in.close();
                return false;
            }
            pair = (int[]) in.readObject();
            in.close();
        }
        catch (Exception e)
        {
            logger.warning("尝试载入缓存文件" + path + "发生异常[" + e + "]，下面将载入源文件并自动缓存……");
            return false;
        }
        return true;
    }

    
    private static int binarySearch(int[] a, int fromIndex, int length, int key)
    {
        int low = fromIndex;
        int high = fromIndex + length - 1;

        while (low <= high)
        {
            int mid = (low + high) >>> 1;
            int midVal = a[mid << 1];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    
    public static int getBiFrequency(String a, String b)
    {
        int idA = CoreDictionary.trie.exactMatchSearch(a);
        if (idA == -1)
        {
            return 0;
        }
        int idB = CoreDictionary.trie.exactMatchSearch(b);
        if (idB == -1)
        {
            return 0;
        }
        int index = binarySearch(pair, start[idA], start[idA + 1] - start[idA], idB);
        if (index < 0) return 0;
        index <<= 1;
        return pair[index + 1];
    }

    
    public static int getBiFrequency(int idA, int idB)
    {
        if (idA == -1)
        {
            return 0;
        }
        if (idB == -1)
        {
            return 0;
        }
        int index = binarySearch(pair, start[idA], start[idA + 1] - start[idA], idB);
        if (index < 0) return 0;
        index <<= 1;
        return pair[index + 1];
    }

    
    public static int getWordID(String a)
    {
        return CoreDictionary.trie.exactMatchSearch(a);
    }
}
