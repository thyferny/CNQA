
package in.thyferny.nlp.collection.AhoCorasick;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import in.thyferny.nlp.corpus.io.ByteArray;


public class AhoCorasickDoubleArrayTrie<V>
{
    
    protected int check[];
    
    protected int base[];
    
    int fail[];
    
    int[][] output;
    
    protected V[] v;

    
    protected int[] l;

    
    protected int size;

    
    public List<Hit<V>> parseText(String text)
    {
        int position = 1;
        int currentState = 0;
        List<Hit<V>> collectedEmits = new LinkedList<Hit<V>>();
        for (int i = 0; i < text.length(); ++i)
        {
            currentState = getState(currentState, text.charAt(i));
            storeEmits(position, currentState, collectedEmits);
            ++position;
        }

        return collectedEmits;
    }

    
    public void parseText(String text, IHit<V> processor)
    {
        int position = 1;
        int currentState = 0;
        for (int i = 0; i < text.length(); ++i)
        {
            currentState = getState(currentState, text.charAt(i));
            int[] hitArray = output[currentState];
            if (hitArray != null)
            {
                for (int hit : hitArray)
                {
                    processor.hit(position - l[hit], position, v[hit]);
                }
            }
            ++position;
        }
    }

    
    public void parseText(char[] text, IHit<V> processor)
    {
        int position = 1;
        int currentState = 0;
        for (char c : text)
        {
            currentState = getState(currentState, c);
            int[] hitArray = output[currentState];
            if (hitArray != null)
            {
                for (int hit : hitArray)
                {
                    processor.hit(position - l[hit], position, v[hit]);
                }
            }
            ++position;
        }
    }

    
    public void parseText(char[] text, IHitFull<V> processor)
    {
        int position = 1;
        int currentState = 0;
        for (char c : text)
        {
            currentState = getState(currentState, c);
            int[] hitArray = output[currentState];
            if (hitArray != null)
            {
                for (int hit : hitArray)
                {
                    processor.hit(position - l[hit], position, v[hit], hit);
                }
            }
            ++position;
        }
    }

    
    public void save(DataOutputStream out) throws Exception
    {
        out.writeInt(size);
        for (int i = 0; i < size; i++)
        {
            out.writeInt(base[i]);
            out.writeInt(check[i]);
            out.writeInt(fail[i]);
            int output[] = this.output[i];
            if (output == null)
            {
                out.writeInt(0);
            }
            else
            {
                out.writeInt(output.length);
                for (int o : output)
                {
                    out.writeInt(o);
                }
            }
        }
        out.writeInt(l.length);
        for (int length : l)
        {
            out.writeInt(length);
        }
    }

    
    public void save(ObjectOutputStream out) throws IOException
    {
        out.writeObject(base);
        out.writeObject(check);
        out.writeObject(fail);
        out.writeObject(output);
        out.writeObject(l);
    }

    
    public void load(ObjectInputStream in, V[] value) throws IOException, ClassNotFoundException
    {
        base = (int[]) in.readObject();
        check = (int[]) in.readObject();
        fail = (int[]) in.readObject();
        output = (int[][]) in.readObject();
        l = (int[]) in.readObject();
        v = value;
    }

    
    public boolean load(ByteArray byteArray, V[] value)
    {
        if (byteArray == null) return false;
        size = byteArray.nextInt();
        base = new int[size + 65535];   // 多留一些，防止越界
        check = new int[size + 65535];
        fail = new int[size + 65535];
        output = new int[size + 65535][];
        int length;
        for (int i = 0; i < size; ++i)
        {
            base[i] = byteArray.nextInt();
            check[i] = byteArray.nextInt();
            fail[i] = byteArray.nextInt();
            length = byteArray.nextInt();
            if (length == 0) continue;
            output[i] = new int[length];
            for (int j = 0; j < output[i].length; ++j)
            {
                output[i][j] = byteArray.nextInt();
            }
        }
        length = byteArray.nextInt();
        l = new int[length];
        for (int i = 0; i < l.length; ++i)
        {
            l[i] = byteArray.nextInt();
        }
        v = value;
        return true;
    }

    
    public V get(String key)
    {
        int index = exactMatchSearch(key);
        if (index >= 0)
        {
            return v[index];
        }

        return null;
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

    
    public interface IHit<V>
    {
        
        void hit(int begin, int end, V value);
    }

    public interface IHitFull<V>
    {
        
        void hit(int begin, int end, V value, int index);
    }

    
    public class Hit<V>
    {
        
        public final int begin;
        
        public final int end;
        
        public final V value;

        public Hit(int begin, int end, V value)
        {
            this.begin = begin;
            this.end = end;
            this.value = value;
        }

        @Override
        public String toString()
        {
            return String.format("[%d:%d]=%s", begin, end, value);
        }
    }

    
    private int getState(int currentState, char character)
    {
        int newCurrentState = transitionWithRoot(currentState, character);  // 先按success跳转
        while (newCurrentState == -1) // 跳转失败的话，按failure跳转
        {
            currentState = fail[currentState];
            newCurrentState = transitionWithRoot(currentState, character);
        }
        return newCurrentState;
    }

    
    private void storeEmits(int position, int currentState, List<Hit<V>> collectedEmits)
    {
        int[] hitArray = output[currentState];
        if (hitArray != null)
        {
            for (int hit : hitArray)
            {
                collectedEmits.add(new Hit<V>(position - l[hit], position, v[hit]));
            }
        }
    }

    
    protected int transition(int current, char c)
    {
        int b = current;
        int p;

        p = b + c + 1;
        if (b == check[p])
            b = base[p];
        else
            return -1;

        p = b;
        return p;
    }

    
    protected int transitionWithRoot(int nodePos, char c)
    {
        int b = base[nodePos];
        int p;

        p = b + c + 1;
        if (b != check[p])
        {
            if (nodePos == 0) return 0;
            return -1;
        }

        return p;
    }


    
    public void build(TreeMap<String, V> map)
    {
        new Builder().build(map);
    }

    
    private int fetch(State parent, List<Map.Entry<Integer, State>> siblings)
    {
        if (parent.isAcceptable())
        {
            State fakeNode = new State(-(parent.getDepth() + 1));  // 此节点是parent的子节点，同时具备parent的输出
            fakeNode.addEmit(parent.getLargestValueId());
            siblings.add(new AbstractMap.SimpleEntry<Integer, State>(0, fakeNode));
        }
        for (Map.Entry<Character, State> entry : parent.getSuccess().entrySet())
        {
            siblings.add(new AbstractMap.SimpleEntry<Integer, State>(entry.getKey() + 1, entry.getValue()));
        }
        return siblings.size();
    }

    
    public int exactMatchSearch(String key)
    {
        return exactMatchSearch(key, 0, 0, 0);
    }

    
    private int exactMatchSearch(String key, int pos, int len, int nodePos)
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

    
    private int exactMatchSearch(char[] keyChars, int pos, int len, int nodePos)
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

//    public void dfs(IWalker walker)
//    {
//        dfs(rootState, "", walker);
//    }
//
//    private void dfs(State currentState, String path, IWalker walker)
//    {
//        walker.meet(path, currentState);
//        for (Character _transition : currentState.getTransitions())
//        {
//            State targetState = currentState.nextState(_transition);
//            dfs(targetState, path + _transition, walker);
//        }
//    }
//
//
//    public static interface IWalker
//    {
//        
//        void meet(String path, State state);
//    }
//

//    public void debug()
//    {
//        System.out.println("base:");
//        for (int i = 0; i < base.length; i++)
//        {
//            if (base[i] < 0)
//            {
//                System.out.println(i + " : " + -base[i]);
//            }
//        }
//
//        System.out.println("output:");
//        for (int i = 0; i < output.length; i++)
//        {
//            if (output[i] != null)
//            {
//                System.out.println(i + " : " + Arrays.toString(output[i]));
//            }
//        }
//
//        System.out.println("fail:");
//        for (int i = 0; i < fail.length; i++)
//        {
//            if (fail[i] != 0)
//            {
//                System.out.println(i + " : " + fail[i]);
//            }
//        }
//
//        System.out.println(this);
//    }

//    @Override
//    public String toString()
//    {
//        String infoIndex = "i    = ";
//        String infoChar = "char = ";
//        String infoBase = "base = ";
//        String infoCheck = "check= ";
//        for (int i = 0; i < Math.min(base.length, 200); ++i)
//        {
//            if (base[i] != 0 || check[i] != 0)
//            {
//                infoChar += "    " + (i == check[i] ? " ×" : (char) (i - check[i] - 1));
//                infoIndex += " " + String.format("%5d", i);
//                infoBase += " " + String.format("%5d", base[i]);
//                infoCheck += " " + String.format("%5d", check[i]);
//            }
//        }
//        return "DoubleArrayTrie：" +
//                "\n" + infoChar +
//                "\n" + infoIndex +
//                "\n" + infoBase +
//                "\n" + infoCheck + "\n" +
////                "check=" + Arrays.toString(check) +
////                ", base=" + Arrays.toString(base) +
////                ", used=" + Arrays.toString(used) +
//                "size=" + size +
//                ", allocSize=" + allocSize +
//                ", keySize=" + keySize +
////                ", length=" + Arrays.toString(length) +
////                ", value=" + Arrays.toString(value) +
//                ", progress=" + progress +
//                ", nextCheckPos=" + nextCheckPos
//                ;
//    }

    
    private static class DebugArray
    {
        Map<String, String> nameValueMap = new LinkedHashMap<String, String>();

        public void add(String name, int value)
        {
            String valueInMap = nameValueMap.get(name);
            if (valueInMap == null)
            {
                valueInMap = "";
            }

            valueInMap += " " + String.format("%5d", value);

            nameValueMap.put(name, valueInMap);
        }

        @Override
        public String toString()
        {
            String text = "";
            for (Map.Entry<String, String> entry : nameValueMap.entrySet())
            {
                String name = entry.getKey();
                String value = entry.getValue();
                text += String.format("%-5s", name) + "= " + value + '\n';
            }

            return text;
        }

        public void println()
        {
            System.out.print(this);
        }
    }

    
    public int size()
    {
        return v.length;
    }

    
    private class Builder
    {
        
        private State rootState = new State();
        
        private boolean used[];
        
        private int allocSize;
        
        private int progress;
        
        private int nextCheckPos;
        
        private int keySize;

        
        @SuppressWarnings("unchecked")
        public void build(TreeMap<String, V> map)
        {
            // 把值保存下来
            v = (V[]) map.values().toArray();
            l = new int[v.length];
            Set<String> keySet = map.keySet();
            // 构建二分trie树
            addAllKeyword(keySet);
            // 在二分trie树的基础上构建双数组trie树
            buildDoubleArrayTrie(keySet);
            used = null;
            // 构建failure表并且合并output表
            constructFailureStates();
            rootState = null;
            loseWeight();
        }

        
        private void addKeyword(String keyword, int index)
        {
            State currentState = this.rootState;
            for (Character character : keyword.toCharArray())
            {
                currentState = currentState.addState(character);
            }
            currentState.addEmit(index);
            l[index] = keyword.length();
        }

        
        private void addAllKeyword(Collection<String> keywordSet)
        {
            int i = 0;
            for (String keyword : keywordSet)
            {
                addKeyword(keyword, i++);
            }
        }

        
        private void constructFailureStates()
        {
            fail = new int[size + 1];
            fail[1] = base[0];
            output = new int[size + 1][];
            Queue<State> queue = new LinkedBlockingDeque<State>();

            // 第一步，将深度为1的节点的failure设为根节点
            for (State depthOneState : this.rootState.getStates())
            {
                depthOneState.setFailure(this.rootState, fail);
                queue.add(depthOneState);
                constructOutput(depthOneState);
            }

            // 第二步，为深度 > 1 的节点建立failure表，这是一个bfs
            while (!queue.isEmpty())
            {
                State currentState = queue.remove();

                for (Character transition : currentState.getTransitions())
                {
                    State targetState = currentState.nextState(transition);
                    queue.add(targetState);

                    State traceFailureState = currentState.failure();
                    while (traceFailureState.nextState(transition) == null)
                    {
                        traceFailureState = traceFailureState.failure();
                    }
                    State newFailureState = traceFailureState.nextState(transition);
                    targetState.setFailure(newFailureState, fail);
                    targetState.addEmit(newFailureState.emit());
                    constructOutput(targetState);
                }
            }
        }

        
        private void constructOutput(State targetState)
        {
            Collection<Integer> emit = targetState.emit();
            if (emit == null || emit.size() == 0) return;
            int output[] = new int[emit.size()];
            Iterator<Integer> it = emit.iterator();
            for (int i = 0; i < output.length; ++i)
            {
                output[i] = it.next();
            }
            AhoCorasickDoubleArrayTrie.this.output[targetState.getIndex()] = output;
        }

        private void buildDoubleArrayTrie(Set<String> keySet)
        {
            progress = 0;
            keySize = keySet.size();
            resize(65536 * 32); // 32个双字节

            base[0] = 1;
            nextCheckPos = 0;

            State root_node = this.rootState;

            List<Map.Entry<Integer, State>> siblings = new ArrayList<Map.Entry<Integer, State>>(root_node.getSuccess().entrySet().size());
            fetch(root_node, siblings);
            insert(siblings);
        }

        
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

        
        private int insert(List<Map.Entry<Integer, State>> siblings)
        {
            int begin = 0;
            int pos = Math.max(siblings.get(0).getKey() + 1, nextCheckPos) - 1;
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

                begin = pos - siblings.get(0).getKey(); // 当前位置离第一个兄弟节点的距离
                if (allocSize <= (begin + siblings.get(siblings.size() - 1).getKey()))
                {
                    // progress can be zero // 防止progress产生除零错误
                    double l = (1.05 > 1.0 * keySize / (progress + 1)) ? 1.05 : 1.0 * keySize / (progress + 1);
                    resize((int) (allocSize * l));
                }

                if (used[begin])
                    continue;

                for (int i = 1; i < siblings.size(); i++)
                    if (check[begin + siblings.get(i).getKey()] != 0)
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

            size = (size > begin + siblings.get(siblings.size() - 1).getKey() + 1) ? size : begin + siblings.get(siblings.size() - 1).getKey() + 1;

            for (Map.Entry<Integer, State> sibling : siblings)
            {
                check[begin + sibling.getKey()] = begin;
            }

            for (Map.Entry<Integer, State> sibling : siblings)
            {
                List<Map.Entry<Integer, State>> new_siblings = new ArrayList<Map.Entry<Integer, State>>(sibling.getValue().getSuccess().entrySet().size() + 1);

                if (fetch(sibling.getValue(), new_siblings) == 0)  // 一个词的终止且不为其他词的前缀，其实就是叶子节点
                {
                    base[begin + sibling.getKey()] = (-sibling.getValue().getLargestValueId() - 1);
                    progress++;
                }
                else
                {
                    int h = insert(new_siblings);   // dfs
                    base[begin + sibling.getKey()] = h;
                }
                sibling.getValue().setIndex(begin + sibling.getKey());
            }
            return begin;
        }

        
        private void loseWeight()
        {
            int nbase[] = new int[size + 65535];
            System.arraycopy(base, 0, nbase, 0, size);
            base = nbase;

            int ncheck[] = new int[size + 65535];
            System.arraycopy(check, 0, ncheck, 0, size);
            check = ncheck;
        }
    }
}
