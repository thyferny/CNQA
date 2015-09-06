
package in.thyferny.nlp.seg.NShort.Path;


public class QueueElement implements Comparable<QueueElement>
{
    
    public int from;
    
    public int index;
    
    public double weight;
    
    public QueueElement next;

    
    public QueueElement(int from, int index, double weight)
    {
        this.from = from;
        this.index = index;
        this.weight = weight;
    }

    @Override
    public int compareTo(QueueElement other)
    {
        return Double.compare(weight, other.weight);
    }
}
