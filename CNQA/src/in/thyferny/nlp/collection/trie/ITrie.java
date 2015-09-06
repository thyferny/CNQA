
package in.thyferny.nlp.collection.trie;

import java.io.DataOutputStream;
import java.util.TreeMap;

import in.thyferny.nlp.corpus.io.ByteArray;


public interface ITrie<V>
{
    int build(TreeMap<String, V> keyValueMap);
    boolean save(DataOutputStream out);
    boolean load(ByteArray byteArray, V[] value);
    V get(char[] key);
    V[] getValueArray(V[] a);
    boolean containsKey(String key);
}
