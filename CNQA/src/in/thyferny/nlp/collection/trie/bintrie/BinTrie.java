
package in.thyferny.nlp.collection.trie.bintrie;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.*;

import in.thyferny.nlp.collection.trie.ITrie;
import in.thyferny.nlp.corpus.io.ByteArray;
import in.thyferny.nlp.corpus.io.IOUtil;
import in.thyferny.nlp.utility.TextUtility;


public class BinTrie<V> extends BaseNode<V> implements ITrie<V>
{
    private int size;

    public BinTrie()
    {
        child = new BaseNode[65535];    // (int)Character.MAX_VALUE
        size = 0;
        status = Status.NOT_WORD_1;
    }

    
    public void put(String key, V value)
    {
        if (key.length() == 0) return;  // 安全起见
        BaseNode branch = this;
        char[] chars = key.toCharArray();
        for (int i = 0; i < chars.length - 1; ++i)
        {
            // 除了最后一个字外，都是继续
            branch.addChild(new Node(chars[i], Status.NOT_WORD_1, null));
            branch = branch.getChild(chars[i]);
        }
        // 最后一个字加入时属性为end
        if (branch.addChild(new Node<V>(chars[chars.length - 1], Status.WORD_END_3, value)))
        {
            ++size; // 维护size
        }
    }

    public void put(char[] key, V value)
    {
        BaseNode branch = this;
        for (int i = 0; i < key.length - 1; ++i)
        {
            // 除了最后一个字外，都是继续
            branch.addChild(new Node(key[i], Status.NOT_WORD_1, null));
            branch = branch.getChild(key[i]);
        }
        // 最后一个字加入时属性为end
        if (branch.addChild(new Node<V>(key[key.length - 1], Status.WORD_END_3, value)))
        {
            ++size; // 维护size
        }
    }

    
    public void remove(String key)
    {
        BaseNode branch = this;
        char[] chars = key.toCharArray();
        for (int i = 0; i < chars.length - 1; ++i)
        {
            if (branch == null) return;
            branch = branch.getChild(chars[i]);
        }
        // 最后一个字设为undefined
        if (branch.addChild(new Node(chars[chars.length - 1], Status.UNDEFINED_0, value)))
        {
            --size;
        }
    }

    public boolean containsKey(String key)
    {
        BaseNode branch = this;
        char[] chars = key.toCharArray();
        for (char aChar : chars)
        {
            if (branch == null) return false;
            branch = branch.getChild(aChar);
        }

        return branch != null && (branch.status == Status.WORD_END_3 || branch.status == Status.WORD_MIDDLE_2);
    }

    public V get(String key)
    {
        BaseNode branch = this;
        char[] chars = key.toCharArray();
        for (char aChar : chars)
        {
            if (branch == null) return null;
            branch = branch.getChild(aChar);
        }

        if (branch == null) return null;
        // 下面这句可以保证只有成词的节点被返回
        if (!(branch.status == Status.WORD_END_3 || branch.status == Status.WORD_MIDDLE_2)) return null;
        return (V) branch.getValue();
    }

    public V get(char[] key)
    {
        BaseNode branch = this;
        for (char aChar : key)
        {
            if (branch == null) return null;
            branch = branch.getChild(aChar);
        }

        if (branch == null) return null;
        // 下面这句可以保证只有成词的节点被返回
        if (!(branch.status == Status.WORD_END_3 || branch.status == Status.WORD_MIDDLE_2)) return null;
        return (V) branch.getValue();
    }

    @Override
    public V[] getValueArray(V[] a)
    {
        if (a.length < size)
            a = (V[]) java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        int i = 0;
        for (Map.Entry<String, V> entry : entrySet())
        {
            a[i++] = entry.getValue();
        }
        return a;
    }

    
    public Set<Map.Entry<String, V>> entrySet()
    {
        Set<Map.Entry<String, V>> entrySet = new TreeSet<Map.Entry<String, V>>();
        StringBuilder sb = new StringBuilder();
        for (BaseNode node : child)
        {
            if (node == null) continue;
            node.walk(new StringBuilder(sb.toString()), entrySet);
        }
        return entrySet;
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

    
    public Set<Map.Entry<String, V>> prefixSearch(String key)
    {
        Set<Map.Entry<String, V>> entrySet = new TreeSet<Map.Entry<String, V>>();
        StringBuilder sb = new StringBuilder(key.substring(0, key.length() - 1));
        BaseNode branch = this;
        char[] chars = key.toCharArray();
        for (char aChar : chars)
        {
            if (branch == null) return entrySet;
            branch = branch.getChild(aChar);
        }

        if (branch == null) return entrySet;
        branch.walk(sb, entrySet);
        return entrySet;
    }

    
    public LinkedList<Map.Entry<String, V>> commonPrefixSearchWithValue(String key)
    {
        char[] chars = key.toCharArray();
        return commonPrefixSearchWithValue(chars, 0);
    }

    
    public LinkedList<Map.Entry<String, V>> commonPrefixSearchWithValue(char[] chars, int begin)
    {
        LinkedList<Map.Entry<String, V>> result = new LinkedList<Map.Entry<String, V>>();
        StringBuilder sb = new StringBuilder();
        BaseNode branch = this;
        for (int i = begin; i < chars.length; ++i)
        {
            char aChar = chars[i];
            branch = branch.getChild(aChar);
            if (branch == null || branch.status == Status.UNDEFINED_0) return result;
            sb.append(aChar);
            if (branch.status == Status.WORD_MIDDLE_2 || branch.status == Status.WORD_END_3)
            {
                result.add(new AbstractMap.SimpleEntry<String, V>(sb.toString(), (V) branch.value));
            }
        }

        return result;
    }

    @Override
    protected boolean addChild(BaseNode node)
    {
        boolean add = false;
        char c = node.getChar();
        BaseNode target = getChild(c);
        if (target == null)
        {
            child[c] = node;
            add = true;
        }
        else
        {
            switch (node.status)
            {
                case UNDEFINED_0:
                    if (target.status != Status.NOT_WORD_1)
                    {
                        target.status = Status.NOT_WORD_1;
                        add = true;
                    }
                    break;
                case NOT_WORD_1:
                    if (target.status == Status.WORD_END_3)
                    {
                        target.status = Status.WORD_MIDDLE_2;
                    }
                    break;
                case WORD_END_3:
                    if (target.status == Status.NOT_WORD_1)
                    {
                        target.status = Status.WORD_MIDDLE_2;
                    }
                    if (target.getValue() == null)
                    {
                        add = true;
                    }
                    target.setValue(node.getValue());
                    break;
            }
        }
        return add;
    }

    public int size()
    {
        return size;
    }

    @Override
    protected char getChar()
    {
        return 0;   // 根节点没有char
    }

    @Override
    public BaseNode getChild(char c)
    {
        return child[c];
    }

    public boolean save(String path)
    {
        try
        {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(path));
            for (BaseNode node : child)
            {
                if (node == null)
                {
                    out.writeInt(0);
                }
                else
                {
                    out.writeInt(1);
                    node.walkToSave(out);
                }
            }
            out.close();
        }
        catch (Exception e)
        {
            logger.warning("保存到" + path + "失败" + TextUtility.exceptionToString(e));
            return false;
        }

        return true;
    }

    @Override
    public int build(TreeMap<String, V> keyValueMap)
    {
        for (Map.Entry<String, V> entry : keyValueMap.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
        return 0;
    }

    
    public boolean save(DataOutputStream out)
    {
        try
        {
            for (BaseNode node : child)
            {
                if (node == null)
                {
                    out.writeInt(0);
                }
                else
                {
                    out.writeInt(1);
                    node.walkToSave(out);
                }
            }
        }
        catch (Exception e)
        {
            logger.warning("保存到" + out + "失败" + TextUtility.exceptionToString(e));
            return false;
        }

        return true;
    }

    
    public boolean load(String path, V[] value)
    {
        byte[] bytes = IOUtil.readBytes(path);
        if (bytes == null) return false;
        _ValueArray valueArray = new _ValueArray(value);
        ByteArray byteArray = new ByteArray(bytes);
        for (int i = 0; i < child.length; ++i)
        {
            int flag = byteArray.nextInt();
            if (flag == 1)
            {
                child[i] = new Node<V>();
                child[i].walkToLoad(byteArray, valueArray);
            }
        }
        size = value.length;

        return true;
    }

    
    public boolean load(String path)
    {
        byte[] bytes = IOUtil.readBytes(path);
        if (bytes == null) return false;
        _ValueArray valueArray = new _EmptyValueArray();
        ByteArray byteArray = new ByteArray(bytes);
        for (int i = 0; i < child.length; ++i)
        {
            int flag = byteArray.nextInt();
            if (flag == 1)
            {
                child[i] = new Node<V>();
                child[i].walkToLoad(byteArray, valueArray);
            }
        }
        size = -1;  // 不知道有多少

        return true;
    }

    public boolean load(ByteArray byteArray, _ValueArray valueArray)
    {
        for (int i = 0; i < child.length; ++i)
        {
            int flag = byteArray.nextInt();
            if (flag == 1)
            {
                child[i] = new Node<V>();
                child[i].walkToLoad(byteArray, valueArray);
            }
        }
        size = valueArray.value.length;

        return true;
    }

    public boolean load(ByteArray byteArray, V[] value)
    {
        return load(byteArray, newValueArray().setValue(value));
    }

    public _ValueArray newValueArray()
    {
        return new _ValueArray();
    }
}
