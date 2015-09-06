
package in.thyferny.nlp.collection.trie.bintrie;


public class _ValueArray<V>
{
    V[] value;
    int offset;

    public _ValueArray(V[] value)
    {
        this.value = value;
    }

    public V nextValue()
    {
        return value[offset++];
    }

    
    protected _ValueArray()
    {
    }

    public _ValueArray setValue(V[] value)
    {
        this.value = value;
        return this;
    }
}
