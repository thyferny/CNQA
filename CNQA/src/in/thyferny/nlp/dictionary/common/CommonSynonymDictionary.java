
package in.thyferny.nlp.dictionary.common;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import in.thyferny.nlp.collection.trie.DoubleArrayTrie;
import in.thyferny.nlp.corpus.synonym.Synonym;
import in.thyferny.nlp.corpus.synonym.SynonymHelper;


public class CommonSynonymDictionary
{
    DoubleArrayTrie<SynonymItem> trie;

    
    private long maxSynonymItemIdDistance;

    private CommonSynonymDictionary()
    {
    }

    public static CommonSynonymDictionary create(InputStream inputStream)
    {
        CommonSynonymDictionary dictionary = new CommonSynonymDictionary();
        if (dictionary.load(inputStream))
        {
            return dictionary;
        }

        return null;
    }

    public boolean load(InputStream inputStream)
    {
        trie = new DoubleArrayTrie<SynonymItem>();
        TreeMap<String, SynonymItem> treeMap = new TreeMap<String, SynonymItem>();
        String line = null;
        try
        {
            BufferedReader bw = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            ArrayList<Synonym> synonymList = null;
            while ((line = bw.readLine()) != null)
            {
                String[] args = line.split(" ");
                synonymList = Synonym.create(args);
                char type = args[0].charAt(args[0].length() - 1);
                for (Synonym synonym : synonymList)
                {
                    treeMap.put(synonym.realWord, new SynonymItem(synonym, synonymList, type));
                    // 这里稍微做个test
                    //assert synonym.getIdString().startsWith(line.split(" ")[0].substring(0, line.split(" ")[0].length() - 1)) : "词典有问题" + line + synonym.toString();
                }
            }
            bw.close();
            // 获取最大语义id
            if (synonymList != null && synonymList.size() > 0)
            {
                maxSynonymItemIdDistance = synonymList.get(synonymList.size() - 1).id - SynonymHelper.convertString2IdWithIndex("Aa01A01", 0) + 1;
            }
            int resultCode = trie.build(treeMap);
            if (resultCode != 0)
            {
                logger.warning("构建" + inputStream + "失败，错误码" + resultCode);
                return false;
            }
        }
        catch (Exception e)
        {
            logger.warning("读取" + inputStream + "失败，可能由行" + line + "造成");
            return false;
        }
        return true;
    }

    public SynonymItem get(String key)
    {
        return trie.get(key);
    }

    
    public long getMaxSynonymItemIdDistance()
    {
        return maxSynonymItemIdDistance;
    }

    
    public long distance(String a, String b)
    {
        SynonymItem itemA = get(a);
        if (itemA == null) return Long.MAX_VALUE / 3;
        SynonymItem itemB = get(b);
        if (itemB == null) return Long.MAX_VALUE / 3;

        return itemA.distance(itemB);
    }

    
    public static class SynonymItem
    {
        
        public Synonym entry;
        
        public List<Synonym> synonymList;

        public static enum Type
        {
            
            EQUAL,
            
            LIKE,
            
            SINGLE,

            
            UNDEFINED,
        }

        
        public Type type;

        public SynonymItem(Synonym entry, List<Synonym> synonymList, Type type)
        {
            this.entry = entry;
            this.synonymList = synonymList;
            this.type = type;
        }

        public SynonymItem(Synonym entry, List<Synonym> synonymList, char type)
        {
            this.entry = entry;
            this.synonymList = synonymList;
            switch (type)
            {
                case '=':
                    this.type = Type.EQUAL;
                    break;
                case '#':
                    this.type = Type.LIKE;
                    break;
                default:
                    this.type = Type.SINGLE;
                    break;
            }
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append(entry);
            sb.append(' ');
            sb.append(type);
            sb.append(' ');
            sb.append(synonymList);
            return sb.toString();
        }

        
        public long distance(SynonymItem other)
        {
            return entry.distance(other.entry);
        }

        
        public static SynonymItem createUndefined(String word)
        {
            SynonymItem item = new SynonymItem(new Synonym(word, word.hashCode() * 1000000 + Long.MAX_VALUE / 3), null, Type.UNDEFINED);
            return item;
        }

    }
}
