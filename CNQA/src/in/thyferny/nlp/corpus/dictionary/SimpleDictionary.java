
package in.thyferny.nlp.corpus.dictionary;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import in.thyferny.nlp.collection.trie.bintrie.BinTrie;


public abstract class SimpleDictionary<V>
{
    BinTrie<V> trie = new BinTrie<V>();

    public boolean load(String path)
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null)
            {
                Map.Entry<String, V> entry = onGenerateEntry(line);
                if (entry == null) continue;
                trie.put(entry.getKey(), entry.getValue());
            }
            br.close();
        }
        catch (Exception e)
        {
            logger.warning("读取" + path + "失败" + e);
            return false;
        }
        return true;
    }

    
    public V get(String key)
    {
        return trie.get(key);
    }

    
    protected abstract Map.Entry<String, V> onGenerateEntry(String line);

    
    public void combine(SimpleDictionary<V> other)
    {
        if (other.trie == null)
        {
            logger.warning("有个词典还没加载");
            return;
        }
        for (Map.Entry<String, V> entry : other.trie.entrySet())
        {
            if (trie.containsKey(entry.getKey())) continue;
            trie.put(entry.getKey(), entry.getValue());
        }
    }
    
    public Set<Map.Entry<String, V>> entrySet()
    {
        return trie.entrySet();
    }

    
    public Set<String> keySet()
    {
        TreeSet<String> keySet = new TreeSet<String>();

        for (Map.Entry<String, V> entry : entrySet())
        {
            keySet.add(entry.getKey());
        }

        return keySet;
    }

    
    public int remove(Filter filter)
    {
        int size = trie.size();
        for (Map.Entry<String, V> entry : entrySet())
        {
            if (filter.remove(entry))
            {
                trie.remove(entry.getKey());
            }
        }

        return size - trie.size();
    }

    public interface Filter<V>
    {
        boolean remove(Map.Entry<String, V> entry);
    }
    
    public void add(String key, V value)
    {
        trie.put(key, value);
    }

    public int size()
    {
        return trie.size();
    }
}
