
package in.thyferny.nlp.seg.Viterbi.Path;

import in.thyferny.nlp.seg.common.Vertex;
import in.thyferny.nlp.utility.MathTools;


public class Node
{
    
    Node from;
    
    double weight;
    
    Vertex vertex;

    public Node(Vertex vertex)
    {
        this.vertex = vertex;
    }

    public void updateFrom(Node from)
    {
        double weight = from.weight + MathTools.calculateWeight(from.vertex, this.vertex);
        if (this.from == null || this.weight > weight)
        {
            this.from = from;
            this.weight = weight;
        }
    }

    @Override
    public String toString()
    {
        return vertex.toString();
    }
}
