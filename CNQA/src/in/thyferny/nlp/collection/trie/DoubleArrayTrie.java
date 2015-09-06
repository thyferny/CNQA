
package in.thyferny.nlp.collection.trie;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

import in.thyferny.nlp.corpus.io.ByteArray;
import in.thyferny.nlp.utility.ByteUtil;


public class DoubleArrayTrie<V> implements Serializable, ITrie<V>
{
    private final static int BUF_SIZE = 16384;
    private final static int UNIT_SIZE = 8; // size of int + int

    private static class Node
    {
        int code;
        int depth;
        int left;
        int right;

        @Override
        public String toString()
        {
            return "Node{" +
                    "code=" + code +
                    ", depth=" + depth +
                    ", left=" + left +
                    ", right=" + right +
                    '}';
        }
    }

    ;

    protected int check[];
    protected int base[];

    private boolean used[];
    
    protected int size;
    private int allocSize;
    private List<String> key;
    private int keySize;
    private int length[];
    private int value[];
    protected V[] v;
    private int progress;
    private int nextCheckPos;
    // boolean no_delete_;
    int error_;

    // int (*progressfunc_) (size_t, size_t);

    // inline _resize expanded

    
    private int resize(int newSize)
    {
        int[] base2 = new int[newSize];
        int[] check2 = new int[newSize];
        boolean used2[] = new boolean[newSize];
        if (allocSize > 0)
        {
            System.arraycopy(base, 0, base2, 0, allocSize);
            System.arraycopy(check, 0, check2, 0, allocSize);
            System.arraycopy(used, 0, used2, 0, allocSize);
        }

        base = base2;
        check = check2;
        used = used2;

        return allocSize = newSize;
    }

    
    private int fetch(Node parent, List<Node> siblings)
    {
        if (error_ < 0)
            return 0;

        int prev = 0;

        for (int i = parent.left; i < parent.right; i++)
        {
            if ((length != null ? length[i] : key.get(i).length()) < parent.depth)
                continue;

            String tmp = key.get(i);

            int cur = 0;
            if ((length != null ? length[i] : tmp.length()) != parent.depth)
                cur = (int) tmp.charAt(parent.depth) + 1;

            if (prev > cur)
            {
                error_ = -3;
                return 0;
            }

            if (cur != prev || siblings.size() == 0)
            {
                Node tmp_node = new Node();
                tmp_node.depth = parent.depth + 1;
                tmp_node.code = cur;
                tmp_node.left = i;
                if (siblings.size() != 0)
                    siblings.get(siblings.size() - 1).right = i;

                siblings.add(tmp_node);
            }

            prev = cur;
        }

        if (siblings.size() != 0)
            siblings.get(siblings.size() - 1).right = parent.right;

        return siblings.size();
    }

    
    private int insert(List<Node> siblings)
    {
        if (error_ < 0)
            return 0;

        int begin = 0;
        int pos = Math.max(siblings.get(0).code + 1, nextCheckPos) - 1;
        int nonzero_num = 0;
        int first = 0;

        if (allocSize <= pos)
            resize(pos + 1);

        outer:
        // 此循环体的目标是找出满足base[begin + a1...an]  == 0的n个空闲空间,a1...an是siblings中的n个节点
        while (true)
        {
            pos++;

            if (allocSize <= pos)
                resize(pos + 1);

            if (check[pos] != 0)
            {
                nonzero_num++;
                continue;
            }
            else if (first == 0)
            {
                nextCheckPos = pos;
                first = 1;
            }

            begin = pos - siblings.get(0).code; // 当前位置离第一个兄弟节点的距离
            if (allocSize <= (begin + siblings.get(siblings.size() - 1).code))
            {
                // progress can be zero // 防止progress产生除零错误
                double l = (1.05 > 1.0 * keySize / (progress + 1)) ? 1.05 : 1.0
                        * keySize / (progress + 1);
                resize((int) (allocSize * l));
            }

            if (used[begin])
                continue;

            for (int i = 1; i < siblings.size(); i++)
                if (check[begin + siblings.get(i).code] != 0)
                    continue outer;

            break;
        }

        // -- Simple heuristics --
        // if the percentage of non-empty contents in check between the
        // index
        // 'next_check_pos' and 'check' is greater than some constant value
        // (e.g. 0.9),
        // new 'next_check_pos' index is written by 'check'.
        if (1.0 * nonzero_num / (pos - nextCheckPos + 1) >= 0.95)
            nextCheckPos = pos; // 从位置 next_check_pos 开始到 pos 间，如果已占用的空间在95%以上，下次插入节点时，直接从 pos 位置处开始查找

        used[begin] = true;
        size = (size > begin + siblings.get(siblings.size() - 1).code + 1) ? size
                : begin + siblings.get(siblings.size() - 1).code + 1;

        for (int i = 0; i < siblings.size(); i++)
        {
            check[begin + siblings.get(i).code] = begin;
//            System.out.println(this);
        }

        for (int i = 0; i < siblings.size(); i++)
        {
            List<Node> new_siblings = new ArrayList<Node>();

            if (fetch(siblings.get(i), new_siblings) == 0)  // 一个词的终止且不为其他词的前缀
            {
                base[begin + siblings.get(i).code] = (value != null) ? (-value[siblings
                        .get(i).left] - 1) : (-siblings.get(i).left - 1);
//                System.out.println(this);

                if (value != null && (-value[siblings.get(i).left] - 1) >= 0)
                {
                    error_ = -2;
                    return 0;
                }

                progress++;
                // if (progress_func_) (*progress_func_) (progress,
                // keySize);
            }
            else
            {
                int h = insert(new_siblings);   // dfs
                base[begin + siblings.get(i).code] = h;
//                System.out.println(this);
            }
        }
        return begin;
    }

    public DoubleArrayTrie()
    {
        check = null;
        base = null;
        used = null;
        size = 0;
        allocSize = 0;
        // no_delete_ = false;
        error_ = 0;
    }

    // no deconstructor

    // set_result omitted
    // the search methods returns (the list of) the value(s) instead
    // of (the list of) the pair(s) of value(s) and length(s)

    // set_array omitted
    // array omitted

    void clear()
    {
        // if (! no_delete_)
        check = null;
        base = null;
        used = null;
        allocSize = 0;
        size = 0;
        // no_delete_ = false;
    }

    public int getUnitSize()
    {
        return UNIT_SIZE;
    }

    public int getSize()
    {
        return size;
    }

    public int getTotalSize()
    {
        return size * UNIT_SIZE;
    }

    public int getNonzeroSize()
    {
        int result = 0;
        for (int i = 0; i < check.length; ++i)
            if (check[i] != 0)
                ++result;
        return result;
    }

    public int build(List<String> key, List<V> value)
    {
        assert key.size() == value.size() : "键的个数与值的个数不一样！";
        assert key.size() > 0 : "键值个数为0！";
        v = (V[]) value.toArray();
        return build(key, null, null, key.size());
    }

    public int build(List<String> key, V[] value)
    {
        assert key.size() == value.length : "键的个数与值的个数不一样！";
        assert key.size() > 0 : "键值个数为0！";
        v = value;
        return build(key, null, null, key.size());
    }

    
    public int build(Set<Map.Entry<String, V>> entrySet)
    {
        List<String> keyList = new ArrayList<String>(entrySet.size());
        List<V> valueList = new ArrayList<V>(entrySet.size());
        for (Map.Entry<String, V> entry : entrySet)
        {
            keyList.add(entry.getKey());
            valueList.add(entry.getValue());
        }

        return build(keyList, valueList);
    }

    
    public int build(TreeMap<String, V> keyValueMap)
    {
        assert keyValueMap != null;
        Set<Map.Entry<String, V>> entrySet = keyValueMap.entrySet();
        return build(entrySet);
    }

    
    public int build(List<String> _key, int _length[], int _value[],
                     int _keySize)
    {
        if (_keySize > _key.size() || _key == null)
            return 0;

        // progress_func_ = progress_func;
        key = _key;
        length = _length;
        keySize = _keySize;
        value = _value;
        progress = 0;

        resize(65536 * 32); // 32个双字节

        base[0] = 1;
        nextCheckPos = 0;

        Node root_node = new Node();
        root_node.left = 0;
        root_node.right = keySize;
        root_node.depth = 0;

        List<Node> siblings = new ArrayList<Node>();
        fetch(root_node, siblings);
        insert(siblings);

        // size += (1 << 8 * 2) + 1; // ???
        // if (size >= allocSize) resize (size);

        used = null;
        key = null;
        length = null;

        return error_;
    }

    public void open(String fileName) throws IOException
    {
        File file = new File(fileName);
        size = (int) file.length() / UNIT_SIZE;
        check = new int[size];
        base = new int[size];

        DataInputStream is = null;
        try
        {
            is = new DataInputStream(new BufferedInputStream(
                    new FileInputStream(file), BUF_SIZE));
            for (int i = 0; i < size; i++)
            {
                base[i] = is.readInt();
                check[i] = is.readInt();
            }
        }
        finally
        {
            if (is != null)
                is.close();
        }
    }

    public boolean save(String fileName)
    {
        DataOutputStream out;
        try
        {
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeInt(size);
            for (int i = 0; i < size; i++)
            {
                out.writeInt(base[i]);
                out.writeInt(check[i]);
            }
            out.close();
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    
    public boolean save(DataOutputStream out)
    {
        try
        {
            out.writeInt(size);
            for (int i = 0; i < size; i++)
            {
                out.writeInt(base[i]);
                out.writeInt(check[i]);
            }
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    public void save(ObjectOutputStream out) throws IOException
    {
        out.writeObject(base);
        out.writeObject(check);
    }

    
    public boolean load(String path, List<V> value)
    {
        if (!loadBaseAndCheck(path)) return false;
        v = (V[]) value.toArray();
        return true;
    }

    
    public boolean load(String path, V[] value)
    {
        if (!loadBaseAndCheckByFileChannel(path)) return false;
        v = value;
        return true;
    }

    public boolean load(ByteArray byteArray, V[] value)
    {
        if (byteArray == null) return false;
        size = byteArray.nextInt();
        base = new int[size + 65535];   // 多留一些，防止越界
        check = new int[size + 65535];
        for (int i = 0; i < size; i++)
        {
            base[i] = byteArray.nextInt();
            check[i] = byteArray.nextInt();
        }
        v = value;
        return true;
    }

    
    public boolean load(String path)
    {
        return loadBaseAndCheckByFileChannel(path);
    }

    
    private boolean loadBaseAndCheck(String path)
    {
        try
        {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
            size = in.readInt();
            base = new int[size + 65535];   // 多留一些，防止越界
            check = new int[size + 65535];
            for (int i = 0; i < size; i++)
            {
                base[i] = in.readInt();
                check[i] = in.readInt();
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    private boolean loadBaseAndCheckByFileChannel(String path)
    {
        try
        {
            FileInputStream fis = new FileInputStream(path);
            // 1.从FileInputStream对象获取文件通道FileChannel
            FileChannel channel = fis.getChannel();
            int fileSize = (int) channel.size();

            // 2.从通道读取文件内容
            ByteBuffer byteBuffer = ByteBuffer.allocate(fileSize);

            // channel.read(ByteBuffer) 方法就类似于 inputstream.read(byte)
            // 每次read都将读取 allocate 个字节到ByteBuffer
            channel.read(byteBuffer);
            // 注意先调用flip方法反转Buffer,再从Buffer读取数据
            byteBuffer.flip();
            // 有几种方式可以操作ByteBuffer
            // 可以将当前Buffer包含的字节数组全部读取出来
            byte[] bytes = byteBuffer.array();
            byteBuffer.clear();
            // 关闭通道和文件流
            channel.close();
            fis.close();

            int index = 0;
            size = ByteUtil.bytesHighFirstToInt(bytes, index);
            index += 4;
            base = new int[size + 65535];   // 多留一些，防止越界
            check = new int[size + 65535];
            for (int i = 0; i < size; i++)
            {
                base[i] = ByteUtil.bytesHighFirstToInt(bytes, index);
                index += 4;
                check[i] = ByteUtil.bytesHighFirstToInt(bytes, index);
                index += 4;
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    
    public boolean serializeTo(String path)
    {
        ObjectOutputStream out = null;
        try
        {
            out = new ObjectOutputStream(new FileOutputStream(path));
            out.writeObject(this);
        }
        catch (Exception e)
        {
//            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static <T> DoubleArrayTrie<T> unSerialize(String path)
    {
        ObjectInputStream in;
        try
        {
            in = new ObjectInputStream(new FileInputStream(path));
            return (DoubleArrayTrie<T>) in.readObject();
        }
        catch (Exception e)
        {
//            e.printStackTrace();
            return null;
        }
    }

    
    public int exactMatchSearch(String key)
    {
        return exactMatchSearch(key, 0, 0, 0);
    }

    public int exactMatchSearch(String key, int pos, int len, int nodePos)
    {
        if (len <= 0)
            len = key.length();
        if (nodePos <= 0)
            nodePos = 0;

        int result = -1;

        char[] keyChars = key.toCharArray();

        int b = base[nodePos];
        int p;

        for (int i = pos; i < len; i++)
        {
            p = b + (int) (keyChars[i]) + 1;
            if (b == check[p])
                b = base[p];
            else
                return result;
        }

        p = b;
        int n = base[p];
        if (b == check[p] && n < 0)
        {
            result = -n - 1;
        }
        return result;
    }

    
    public int exactMatchSearch(char[] keyChars, int pos, int len, int nodePos)
    {
        int result = -1;

        int b = base[nodePos];
        int p;

        for (int i = pos; i < len; i++)
        {
            p = b + (int) (keyChars[i]) + 1;
            if (b == check[p])
                b = base[p];
            else
                return result;
        }

        p = b;
        int n = base[p];
        if (b == check[p] && n < 0)
        {
            result = -n - 1;
        }
        return result;
    }

    public List<Integer> commonPrefixSearch(String key)
    {
        return commonPrefixSearch(key, 0, 0, 0);
    }

    
    public List<Integer> commonPrefixSearch(String key, int pos, int len, int nodePos)
    {
        if (len <= 0)
            len = key.length();
        if (nodePos <= 0)
            nodePos = 0;

        List<Integer> result = new ArrayList<Integer>();

        char[] keyChars = key.toCharArray();

        int b = base[nodePos];
        int n;
        int p;

        for (int i = pos; i < len; i++)
        {
            p = b + (int) (keyChars[i]) + 1;    // 状态转移 p = base[char[i-1]] + char[i] + 1
            if (b == check[p])                  // base[char[i-1]] == check[base[char[i-1]] + char[i] + 1]
                b = base[p];
            else
                return result;
            p = b;
            n = base[p];
            if (b == check[p] && n < 0)         // base[p] == check[p] && base[p] < 0 查到一个词
            {
                result.add(-n - 1);
            }
        }

        return result;
    }

    
    public LinkedList<Map.Entry<String, V>> commonPrefixSearchWithValue(String key)
    {
        int len = key.length();
        LinkedList<Map.Entry<String, V>> result = new LinkedList<Map.Entry<String, V>>();
        char[] keyChars = key.toCharArray();
        int b = base[0];
        int n;
        int p;

        for (int i = 0; i < len; ++i)
        {
            p = b;
            n = base[p];
            if (b == check[p] && n < 0)         // base[p] == check[p] && base[p] < 0 查到一个词
            {
                result.add(new AbstractMap.SimpleEntry<String, V>(new String(keyChars, 0, i), v[-n - 1]));
            }

            p = b + (int) (keyChars[i]) + 1;    // 状态转移 p = base[char[i-1]] + char[i] + 1
            // 下面这句可能产生下标越界，不如改为if (p < size && b == check[p])，或者多分配一些内存
            if (b == check[p])                  // base[char[i-1]] == check[base[char[i-1]] + char[i] + 1]
                b = base[p];
            else
                return result;
        }

        p = b;
        n = base[p];

        if (b == check[p] && n < 0)
        {
            result.add(new AbstractMap.SimpleEntry<String, V>(key, v[-n - 1]));
        }

        return result;
    }

    
    public LinkedList<Map.Entry<String, V>> commonPrefixSearchWithValue(char[] keyChars, int begin)
    {
        int len = keyChars.length;
        LinkedList<Map.Entry<String, V>> result = new LinkedList<Map.Entry<String, V>>();
        int b = base[0];
        int n;
        int p;

        for (int i = begin; i < len; ++i)
        {
            p = b;
            n = base[p];
            if (b == check[p] && n < 0)         // base[p] == check[p] && base[p] < 0 查到一个词
            {
                result.add(new AbstractMap.SimpleEntry<String, V>(new String(keyChars, begin, i - begin), v[-n - 1]));
            }

            p = b + (int) (keyChars[i]) + 1;    // 状态转移 p = base[char[i-1]] + char[i] + 1
            // 下面这句可能产生下标越界，不如改为if (p < size && b == check[p])，或者多分配一些内存
            if (b == check[p])                  // base[char[i-1]] == check[base[char[i-1]] + char[i] + 1]
                b = base[p];
            else
                return result;
        }

        p = b;
        n = base[p];

        if (b == check[p] && n < 0)
        {
            result.add(new AbstractMap.SimpleEntry<String, V>(new String(keyChars, begin, len - begin), v[-n - 1]));
        }

        return result;
    }

    @Override
    public String toString()
    {
//        String infoIndex    = "i    = ";
//        String infoChar     = "char = ";
//        String infoBase     = "base = ";
//        String infoCheck    = "check= ";
//        for (int i = 0; i < base.length; ++i)
//        {
//            if (base[i] != 0 || check[i] != 0)
//            {
//                infoChar  += "    " + (i == check[i] ? " ×" : (char)(i - check[i] - 1));
//                infoIndex += " " + String.format("%5d", i);
//                infoBase  += " " +  String.format("%5d", base[i]);
//                infoCheck += " " + String.format("%5d", check[i]);
//            }
//        }
        return "DoubleArrayTrie{" +
//                "\n" + infoChar +
//                "\n" + infoIndex +
//                "\n" + infoBase +
//                "\n" + infoCheck + "\n" +
//                "check=" + Arrays.toString(check) +
//                ", base=" + Arrays.toString(base) +
//                ", used=" + Arrays.toString(used) +
                "size=" + size +
                ", allocSize=" + allocSize +
                ", key=" + key +
                ", keySize=" + keySize +
//                ", length=" + Arrays.toString(length) +
//                ", value=" + Arrays.toString(value) +
                ", progress=" + progress +
                ", nextCheckPos=" + nextCheckPos +
                ", error_=" + error_ +
                '}';
    }

    
    public int size()
    {
        return v.length;
    }

    
    public int[] getCheck()
    {
        return check;
    }

    
    public int[] getBase()
    {
        return base;
    }

    
    public V getValueAt(int index)
    {
        return v[index];
    }

    
    public V get(String key)
    {
        int index = exactMatchSearch(key);
        if (index >= 0)
        {
            return getValueAt(index);
        }

        return null;
    }

    public V get(char[] key)
    {
        int index = exactMatchSearch(key, 0, key.length, 0);
        if (index >= 0)
        {
            return getValueAt(index);
        }

        return null;
    }

    public V[] getValueArray(V[] a)
    {
        // I hate this but just have to
        int size = v.length;
        if (a.length < size)
            a = (V[]) java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        System.arraycopy(v, 0, a, 0, size);
        return a;
    }

    public boolean containsKey(String key)
    {
        return exactMatchSearch(key) >= 0;
    }

    
    protected int transition(String path)
    {
        return transition(path.toCharArray());
    }

    
    protected int transition(char[] path)
    {
        int b = base[0];
        int p;

        for (int i = 0; i < path.length; ++i)
        {
            p = b + (int) (path[i]) + 1;
            if (b == check[p])
                b = base[p];
            else
                return -1;
        }

        p = b;
        return p;
    }

    
    public int transition(String path, int from)
    {
        int b = from;
        int p;

        for (int i = 0; i < path.length(); ++i)
        {
            p = b + (int) (path.charAt(i)) + 1;
            if (b == check[p])
                b = base[p];
            else
                return -1;
        }

        p = b;
        return p;
    }

    
    public V output(int state)
    {
        if (state < 0) return null;
        int n = base[state];
        if (state == check[state] && n < 0)
        {
            return v[-n - 1];
        }
        return null;
    }

    
    public class Searcher
    {
        
        public int begin;
        
        public int length;
        
        public int index;
        
        public V value;
        
        private char[] charArray;
        
        private int last;
        
        private int i;
        
        private int arrayLength;

        
        public Searcher(int offset, char[] charArray)
        {
            this.charArray = charArray;
            i = offset;
            last = base[0];
            arrayLength = charArray.length;
            // A trick，如果文本长度为0的话，调用next()时，会带来越界的问题。
            // 所以我要在第一次调用next()的时候触发begin == arrayLength进而返回false。
            // 当然也可以改成begin >= arrayLength，不过我觉得操作符>=的效率低于==
            if (arrayLength == 0) begin = -1;
            else begin = offset;
        }

        
        public boolean next()
        {
            int b = last;
            int n;
            int p;

            for (; ; ++i)
            {
                if (i == arrayLength)               // 指针到头了，将起点往前挪一个，重新开始，状态归零
                {
                    ++begin;
                    if (begin == arrayLength) break;
                    i = begin;
                    b = base[0];
                }
                p = b + (int) (charArray[i]) + 1;   // 状态转移 p = base[char[i-1]] + char[i] + 1
                if (b == check[p])                  // base[char[i-1]] == check[base[char[i-1]] + char[i] + 1]
                    b = base[p];                    // 转移成功
                else
                {
                    i = begin;                      // 转移失败，也将起点往前挪一个，重新开始，状态归零
                    ++begin;
                    if (begin == arrayLength) break;
                    b = base[0];
                    continue;
                }
                p = b;
                n = base[p];
                if (b == check[p] && n < 0)         // base[p] == check[p] && base[p] < 0 查到一个词
                {
                    length = i - begin + 1;
                    index = -n - 1;
                    value = v[index];
                    last = b;
                    ++i;
                    return true;
                }
            }

            return false;
        }
    }

    public Searcher getSearcher(String text, int offset)
    {
        return new Searcher(offset, text.toCharArray());
    }

    public Searcher getSearcher(char[] text, int offset)
    {
        return new Searcher(offset, text);
    }

    
    protected int transition(int current, char c)
    {
        int b = base[current];
        int p;

        p = b + c + 1;
        if (b == check[p])
            b = base[p];
        else
            return -1;

        p = b;
        return p;
    }

    
    public boolean set(String key, V value)
    {
        int index = exactMatchSearch(key);
        if (index >= 0)
        {
            v[index] = value;
            return true;
        }

        return false;
    }

    
    public V get(int index)
    {
        return v[index];
    }


    
//    public void report()
//    {
//        System.out.println("size: " + size);
//        int nonZeroIndex = 0;
//        for (int i = 0; i < base.length; i++)
//        {
//            if (base[i] != 0) nonZeroIndex = i;
//        }
//        System.out.println("BaseUsed: " + nonZeroIndex);
//        nonZeroIndex = 0;
//        for (int i = 0; i < check.length; i++)
//        {
//            if (check[i] != 0) nonZeroIndex = i;
//        }
//        System.out.println("CheckUsed: " + nonZeroIndex);
//    }
}