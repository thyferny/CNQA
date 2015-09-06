
package in.thyferny.nlp.dictionary;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.collection.trie.DoubleArrayTrie;
import in.thyferny.nlp.collection.trie.bintrie.BinTrie;
import in.thyferny.nlp.corpus.io.ByteArray;
import in.thyferny.nlp.corpus.io.IOUtil;
import in.thyferny.nlp.utility.Predefine;
import in.thyferny.nlp.utility.TextUtility;


public class BiGramDictionary
{
    static DoubleArrayTrie<Integer> trie;

    public final static String path = MyNLP.Config.BiGramDictionaryPath;
    public static final int totalFrequency = 37545990;

    // 自动加载词典
    static
    {
        long start = System.currentTimeMillis();
        if (!load(path))
        {
            logger.severe("二元词典加载失败");
            System.exit(-1);
        }
        else
        {
            logger.info(path + "加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
        }
    }

    public static boolean load(String path)
    {
        logger.info("二元词典开始加载:" + path);
        trie = new DoubleArrayTrie<Integer>();
        boolean create = !loadDat(path);
        if (!create) return true;
        TreeMap<String, Integer> map = new TreeMap<String, Integer>();
        BufferedReader br;
        try
        {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null)
            {
                String[] params = line.split("\\s");
                String twoWord = params[0];
                int freq = Integer.parseInt(params[1]);
                map.put(twoWord, freq);
            }
            br.close();
            logger.info("二元词典读取完毕:" + path + "，开始构建双数组Trie树(DoubleArrayTrie)……");
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

        int resultCode = trie.build(map);
        logger.info("二元词典DAT构建结果:{}" + resultCode);
//        reSaveDictionary(map, path);
        logger.info("二元词典加载成功:" + trie.size() + "个词条");
        if (create)
        {
            try
            {
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path + Predefine.BIN_EXT)));
                Collection<Integer> freqList = map.values();
                out.writeInt(freqList.size());
                for (int freq : freqList)
                {
                    out.writeInt(freq);
                }
                trie.save(out);
                out.close();
            }
            catch (Exception e)
            {
                logger.warning("在缓存" + path + Predefine.BIN_EXT + "时发生异常" + TextUtility.exceptionToString(e));
                return false;
            }
        }
        return true;
    }

    
    private static boolean loadDat(String path)
    {
        try
        {
            ByteArray byteArray = ByteArray.createByteArray(path + Predefine.BIN_EXT);
            if (byteArray == null) return false;

            int size = byteArray.nextInt();
            Integer[] value = new Integer[size];
            for (int i = 0; i < size; i++)
            {
                value[i] = byteArray.nextInt();
            }
            if (!trie.load(byteArray, value)) return false;
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    
    public static Set<String> _findSpecialString()
    {
        Set<String> stringSet = new HashSet<String>();
        BufferedReader br;
        try
        {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null)
            {
                String[] params = line.split("\t");
                String twoWord = params[0];
                params = twoWord.split("@");
                for (String w : params)
                {
                    if (w.contains("##"))
                    {
                        stringSet.add(w);
                    }
                }
            }
            br.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return stringSet;
    }

    
    public static int getBiFrequency(String from, String to)
    {
        return getBiFrequency(from + '@' + to);
    }

    
    public static int getBiFrequency(String twoWord)
    {
        Integer result = trie.get(twoWord);
        return (result == null ? 0 : result);
    }

    
    private static boolean reSaveDictionary(TreeMap<String, Integer> map, String path)
    {
        StringBuilder sbOut = new StringBuilder();
        for (Map.Entry<String, Integer> entry : map.entrySet())
        {
            sbOut.append(entry.getKey());
            sbOut.append(' ');
            sbOut.append(entry.getValue());
            sbOut.append('\n');
        }

        return IOUtil.saveTxt(path, sbOut.toString());
    }

    
    private static void sortListForBuildTrie(List<String> wordList, List<Integer> freqList, String path)
    {
        BinTrie<Integer> binTrie = new BinTrie<Integer>();
        for (int i = 0; i < wordList.size(); ++i)
        {
            binTrie.put(wordList.get(i), freqList.get(i));
        }
        Collections.sort(wordList);
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
//            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "_sort.txt")));
            for (String w : wordList)
            {
                bw.write(w + '\t' + binTrie.get(w));
                bw.newLine();
            }
            bw.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
