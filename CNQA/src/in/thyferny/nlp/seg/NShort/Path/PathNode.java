
package in.thyferny.nlp.seg.NShort.Path;


public class PathNode
{
    
    public int from;
    
    public int index;

    
    public PathNode(int from, int index)
    {
        this.from = from;
        this.index = index;
    }

    @Override
    public String toString()
    {
        return "PathNode{" +
                "from=" + from +
                ", index=" + index +
                '}';
    }
}
