
package in.thyferny.nlp.corpus.dictionary;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

import in.thyferny.nlp.corpus.io.IOUtil;
import in.thyferny.nlp.corpus.occurrence.TermFrequency;


public class TFDictionary extends SimpleDictionary<TermFrequency> implements ISaveAble
{
    String delimeter;

    public TFDictionary(String delimeter)
    {
        this.delimeter = delimeter;
    }

    public TFDictionary()
    {
        this("=");
    }

    @Override
    protected Map.Entry<String, TermFrequency> onGenerateEntry(String line)
    {
        String[] param = line.split(delimeter);
        return new AbstractMap.SimpleEntry<String, TermFrequency>(param[0], new TermFrequency(param[0], Integer.valueOf(param[1])));
    }

    public int combine(TFDictionary dictionary, int limit, boolean add)
    {
        int preSize = trie.size();
        for (Map.Entry<String, TermFrequency> entry : dictionary.trie.entrySet())
        {
            TermFrequency termFrequency = trie.get(entry.getKey());
            if (termFrequency == null)
            {
                trie.put(entry.getKey(), new TermFrequency(entry.getKey(), Math.min(limit, entry.getValue().getValue())));
            }
            else
            {
                if (add)
                {
                    termFrequency.setValue(termFrequency.getValue() + Math.min(limit, entry.getValue().getValue()));
                }
            }
        }
        return trie.size() - preSize;
    }

    public static int combine(String[] path)
    {
        TFDictionary dictionaryMain = new TFDictionary();
        dictionaryMain.load(path[0]);
        int preSize = dictionaryMain.trie.size();
        for (int i = 1; i < path.length; ++i)
        {
            TFDictionary dictionary = new TFDictionary();
            dictionary.load(path[i]);
            dictionaryMain.combine(dictionary, 1, true);
        }
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path[0])));
            for (Map.Entry<String, TermFrequency> entry : dictionaryMain.trie.entrySet())
            {
                bw.write(entry.getKey());
                bw.write(' ');
                bw.write(String.valueOf(entry.getValue().getValue()));
                bw.newLine();
            }
            bw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }

        return dictionaryMain.trie.size() - preSize;
    }

    
    public int getFrequency(String key)
    {
        TermFrequency termFrequency = get(key);
        if (termFrequency == null) return 0;
        return termFrequency.getFrequency();
    }

    public void add(String key)
    {
        TermFrequency termFrequency = trie.get(key);
        if (termFrequency == null)
        {
            termFrequency = new TermFrequency(key);
            trie.put(key, termFrequency);
        }
        else
        {
            termFrequency.increase();
        }
    }

    @Override
    public boolean saveTxtTo(String path)
    {
        if ("=".equals(delimeter))
        {
            LinkedList<TermFrequency> termFrequencyLinkedList = new LinkedList<TermFrequency>();
            for (Map.Entry<String, TermFrequency> entry : trie.entrySet())
            {
                termFrequencyLinkedList.add(entry.getValue());
            }
            return IOUtil.saveCollectionToTxt(termFrequencyLinkedList, path);
        }
        else
        {
            ArrayList<String> outList = new ArrayList<String>(size());
            for (Map.Entry<String, TermFrequency> entry : trie.entrySet())
            {
                outList.add(entry.getKey() + delimeter + entry.getValue().getFrequency());
            }
            return IOUtil.saveCollectionToTxt(outList, path);
        }
    }

    
    public boolean saveKeyTo(String path)
    {
        LinkedList<String> keyList = new LinkedList<String>();
        for (Map.Entry<String, TermFrequency> entry : trie.entrySet())
        {
            keyList.add(entry.getKey());
        }
        return IOUtil.saveCollectionToTxt(keyList, path);
    }

    
    public TreeSet<TermFrequency> values()
    {
        TreeSet<TermFrequency> set = new TreeSet<TermFrequency>(Collections.reverseOrder());

        for (Map.Entry<String, TermFrequency> entry : entrySet())
        {
            set.add(entry.getValue());
        }

        return set;
    }
}
