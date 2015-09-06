
package in.thyferny.nlp.corpus.dictionary;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.*;
import java.util.*;

import in.thyferny.nlp.collection.trie.bintrie.BinTrie;
import in.thyferny.nlp.corpus.dictionary.item.Item;
import in.thyferny.nlp.corpus.document.sentence.word.IWord;
import in.thyferny.nlp.corpus.document.sentence.word.Word;


public class DictionaryMaker implements ISaveAble
{
    BinTrie<Item> trie;

    public DictionaryMaker()
    {
        trie = new BinTrie<Item>();
    }

    
    public void add(IWord word)
    {
        Item item = trie.get(word.getValue());
        if (item == null)
        {
            item = new Item(word.getValue(), word.getLabel());
            trie.put(item.key, item);
        }
        else
        {
            item.addLabel(word.getLabel());
        }
    }

    public void add(String value, String label)
    {
        add(new Word(value, label));
    }

    public Item get(String key)
    {
        return trie.get(key);
    }

    public Item get(IWord word)
    {
        return get(word.getValue());
    }

    public TreeSet<String> labelSet()
    {
        TreeSet<String> labelSet = new TreeSet<String>();
        for (Map.Entry<String, Item> entry : entrySet())
        {
            labelSet.addAll(entry.getValue().labelMap.keySet());
        }

        return labelSet;
    }

    
    public static List<Item> loadAsItemList(String path)
    {
        List<Item> itemList = new LinkedList<Item>();
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null)
            {
                Item item = Item.create(line);
                if (item == null)
                {
                    logger.warning("使用【" + line + "】创建Item失败");
                    return null;
//                    continue;
                }
                itemList.add(item);
            }
        }
        catch (Exception e)
        {
            logger.warning("读取词典" + path + "发生异常" + e);
            return null;
        }

        return itemList;
    }

    
    public static DictionaryMaker load(String path)
    {
        DictionaryMaker dictionaryMaker = new DictionaryMaker();
        dictionaryMaker.addAll(DictionaryMaker.loadAsItemList(path));

        return dictionaryMaker;
    }

    
    public void addAll(List<Item> itemList)
    {
        for (Item item : itemList)
        {
            add(item);
        }
    }

    
    public void addAllNotCombine(List<Item> itemList)
    {
        for (Item item : itemList)
        {
            addNotCombine(item);
        }
    }

    
    public void add(Item item)
    {
        Item innerItem = trie.get(item.key);
        if (innerItem == null)
        {
            innerItem = item;
            trie.put(innerItem.key, innerItem);
        }
        else
        {
            innerItem.combine(item);
        }
    }

    
    public Set<Map.Entry<String, Item>> entrySet()
    {
        return trie.entrySet();
    }

    public Set<String> keySet()
    {
        return trie.keySet();
    }

    
    public void addNotCombine(Item item)
    {
        Item innerItem = trie.get(item.key);
        if (innerItem == null)
        {
            innerItem = item;
            trie.put(innerItem.key, innerItem);
        }
    }

    
    public static DictionaryMaker combine(String pathA, String pathB)
    {
        DictionaryMaker dictionaryMaker = new DictionaryMaker();
        dictionaryMaker.addAll(DictionaryMaker.loadAsItemList(pathA));
        dictionaryMaker.addAll(DictionaryMaker.loadAsItemList(pathB));

        return dictionaryMaker;
    }

    
    public static DictionaryMaker combine(String[] pathArray)
    {
        DictionaryMaker dictionaryMaker = new DictionaryMaker();
        for (String path : pathArray)
        {
            logger.warning("正在处理" + path);
            dictionaryMaker.addAll(DictionaryMaker.loadAsItemList(path));
        }
        return dictionaryMaker;
    }

    
    public static DictionaryMaker combineWithNormalization(String[] pathArray)
    {
        DictionaryMaker dictionaryMaker = new DictionaryMaker();
        logger.info("正在处理主词典" + pathArray[0]);
        dictionaryMaker.addAll(DictionaryMaker.loadAsItemList(pathArray[0]));
        for (int i = 1; i < pathArray.length; ++i)
        {
            logger.info("正在处理副词典" + pathArray[i] + "，将执行新词合并模式");
            dictionaryMaker.addAllNotCombine(DictionaryMaker.loadAsItemList(pathArray[i]));
        }
        return dictionaryMaker;
    }

    
    public static DictionaryMaker combineWhenNotInclude(String[] pathArray)
    {
        DictionaryMaker dictionaryMaker = new DictionaryMaker();
        logger.info("正在处理主词典" + pathArray[0]);
        dictionaryMaker.addAll(DictionaryMaker.loadAsItemList(pathArray[0]));
        for (int i = 1; i < pathArray.length; ++i)
        {
            logger.info("正在处理副词典" + pathArray[i] + "，并且过滤已有词典");
            dictionaryMaker.addAllNotCombine(DictionaryMaker.normalizeFrequency(DictionaryMaker.loadAsItemList(pathArray[i])));
        }
        return dictionaryMaker;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("词条数量：");
        sb.append(trie.size());
        return sb.toString();
    }

    @Override
    public boolean saveTxtTo(String path)
    {
        if (trie.size() == 0) return true;  // 如果没有词条，那也算成功了
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
            Set<Map.Entry<String, Item>> entries = trie.entrySet();
            for (Map.Entry<String, Item> entry : entries)
            {
                bw.write(entry.getValue().toString());
                bw.newLine();
            }
            bw.close();
        }
        catch (Exception e)
        {
            logger.warning("保存到" + path + "失败" + e);
            return false;
        }

        return true;
    }

    public void add(String param)
    {
        Item item = Item.create(param);
        if (item != null) add(item);
    }

    public static interface Filter
    {
        
        boolean onSave(Item item);
    }

    
    public boolean saveTxtTo(String path, Filter filter)
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
            Set<Map.Entry<String, Item>> entries = trie.entrySet();
            for (Map.Entry<String, Item> entry : entries)
            {
                if (filter.onSave(entry.getValue()))
                {
                    bw.write(entry.getValue().toString());
                    bw.newLine();
                }
            }
            bw.close();
        }
        catch (Exception e)
        {
            logger.warning("保存到" + path + "失败" + e);
            return false;
        }

        return true;
    }

    
    public static List<Item> normalizeFrequency(List<Item> itemList)
    {
        for (Item item : itemList)
        {
            ArrayList<Map.Entry<String, Integer>> entryArray = new ArrayList<Map.Entry<String, Integer>>(item.labelMap.entrySet());
            Collections.sort(entryArray, new Comparator<Map.Entry<String, Integer>>()
            {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
            });
            int index = 1;
            for (Map.Entry<String, Integer> pair : entryArray)
            {
                item.labelMap.put(pair.getKey(), index);
                ++index;
            }
        }
        return itemList;
    }
}
