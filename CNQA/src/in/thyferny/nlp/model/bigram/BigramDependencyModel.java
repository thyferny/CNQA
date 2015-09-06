
package in.thyferny.nlp.model.bigram;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.TreeMap;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.collection.trie.DoubleArrayTrie;
import in.thyferny.nlp.corpus.dependency.model.WordNatureWeightModelMaker;
import in.thyferny.nlp.corpus.io.ByteArray;
import in.thyferny.nlp.corpus.io.IOUtil;
import in.thyferny.nlp.utility.Predefine;


public class BigramDependencyModel
{
    static DoubleArrayTrie<String> trie;

    static
    {
        long start = System.currentTimeMillis();
        if (load(MyNLP.Config.WordNatureModelPath))
        {
            logger.info("加载依存句法二元模型" + MyNLP.Config.WordNatureModelPath + "成功，耗时：" + (System.currentTimeMillis() - start) + " ms");
        }
        else
        {
            logger.warning("加载依存句法二元模型" + MyNLP.Config.WordNatureModelPath + "失败，耗时：" + (System.currentTimeMillis() - start) + " ms");
        }
    }

    static boolean load(String path)
    {
        trie = new DoubleArrayTrie<String>();
        if (loadDat(path + ".bi" + Predefine.BIN_EXT)) return true;
        TreeMap<String, String> map = new TreeMap<String, String>();
        for (String line : IOUtil.readLineListWithLessMemory(path))
        {
            String[] param = line.split(" ");
            if (param[0].endsWith("@"))
            {
                continue;
            }
            String dependency = param[1];
            map.put(param[0], dependency);
        }
        if (map.size() == 0) return false;
        trie.build(map);
        if (!saveDat(path, map)) logger.warning("缓存" + path + Predefine.BIN_EXT + "失败");
        return true;
    }

    private static boolean loadDat(String path)
    {
        ByteArray byteArray = ByteArray.createByteArray(path);
        if (byteArray == null) return false;
        int size = byteArray.nextInt();
        String[] valueArray = new String[size];
        for (int i = 0; i < valueArray.length; ++i)
        {
            valueArray[i] = byteArray.nextUTF();
        }
        return trie.load(byteArray, valueArray);
    }

    static boolean saveDat(String path, TreeMap<String, String> map)
    {
        Collection<String> dependencyList = map.values();
        // 缓存值文件
        try
        {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(path +  ".bi" + Predefine.BIN_EXT));
            out.writeInt(dependencyList.size());
            for (String dependency : dependencyList)
            {
                out.writeUTF(dependency);
            }
            if (!trie.save(out)) return false;
            out.close();
        }
        catch (Exception e)
        {
            logger.warning("保存失败" + e);
            return false;
        }
        return true;
    }

    public static String get(String key)
    {
        return trie.get(key);
    }

    
    public static String get(String fromWord, String fromPos, String toWord, String toPos)
    {
        String dependency = get(fromWord + "@" + toWord);
        if (dependency == null) dependency = get(fromWord + "@" + WordNatureWeightModelMaker.wrapTag(toPos));
        if (dependency == null) dependency = get(WordNatureWeightModelMaker.wrapTag(fromPos) + "@" + toWord);
        if (dependency == null) dependency = get(WordNatureWeightModelMaker.wrapTag(fromPos) + "@" + WordNatureWeightModelMaker.wrapTag(toPos));
        if (dependency == null) dependency = "未知";

        return dependency;
    }
}
