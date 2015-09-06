
package in.thyferny.nlp.model.trigram.frequency;

import java.io.DataOutputStream;
import java.util.Collection;
import java.util.Set;

import in.thyferny.nlp.collection.trie.bintrie.BinTrie;
import in.thyferny.nlp.corpus.io.ByteArray;
import in.thyferny.nlp.corpus.io.ICacheAble;


public class Probability implements ICacheAble
{
    public BinTrie<Integer> d;
    int total;

    public Probability()
    {
        d = new BinTrie<Integer>();
    }

    public boolean exists(String key)
    {
        return d.containsKey(key);
    }

    public int getsum()
    {
        return total;
    }

    Integer get(String key)
    {
        return d.get(key);
    }

    public int get(char[]... keyArray)
    {
        Integer f = get(convert(keyArray));
        if (f == null) return 0;
        return f;
    }

    public int get(char... key)
    {
        Integer f = d.get(key);
        if (f == null) return 0;
        return f;
    }

    public double freq(String key)
    {
        Integer f = get(key);
        if (f == null) f = 0;
        return f / (double) total;
    }

    public double freq(char[]... keyArray)
    {
        return freq(convert(keyArray));
    }

    public double freq(char... keyArray)
    {
        Integer f = d.get(keyArray);
        if (f == null) f = 0;
        return f / (double) total;
    }

    public Set<String> samples()
    {
        return d.keySet();
    }

    void add(String key, int value)
    {
        Integer f = get(key);
        if (f == null) f = 0;
        f += value;
        d.put(key, f);
        total += value;
    }

    void add(int value, char... key)
    {
        Integer f = d.get(key);
        if (f == null) f = 0;
        f += value;
        d.put(key, f);
        total += value;
    }

    public void add(int value, char[]... keyArray)
    {
        add(convert(keyArray), value);
    }

    public void add(int value, Collection<char[]> keyArray)
    {
        add(convert(keyArray), value);
    }

    private String convert(Collection<char[]> keyArray)
    {
        StringBuilder sbKey = new StringBuilder(keyArray.size() * 2);
        for (char[] key : keyArray)
        {
            sbKey.append(key[0]);
            sbKey.append(key[1]);
        }
        return sbKey.toString();
    }

    static private String convert(char[]... keyArray)
    {
        StringBuilder sbKey = new StringBuilder(keyArray.length * 2);
        for (char[] key : keyArray)
        {
            sbKey.append(key[0]);
            sbKey.append(key[1]);
        }
        return sbKey.toString();
    }

    @Override
    public void save(DataOutputStream out) throws Exception
    {
        out.writeInt(total);
        Integer[] valueArray = d.getValueArray(new Integer[0]);
        out.writeInt(valueArray.length);
        for (Integer v : valueArray)
        {
            out.writeInt(v);
        }
        d.save(out);
    }

    @Override
    public boolean load(ByteArray byteArray)
    {
        total = byteArray.nextInt();
        int size = byteArray.nextInt();
        Integer[] valueArray = new Integer[size];
        for (int i = 0; i < valueArray.length; ++i)
        {
            valueArray[i] = byteArray.nextInt();
        }
        d.load(byteArray, valueArray);
        return true;
    }
}
