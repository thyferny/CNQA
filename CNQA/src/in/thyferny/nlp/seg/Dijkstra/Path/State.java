
package in.thyferny.nlp.seg.Dijkstra.Path;

import in.thyferny.nlp.seg.common.Vertex;


public class State implements Comparable<State>
{
    
    public double cost;
    
    public int vertex;

    @Override
    public int compareTo(State o)
    {
        return Double.compare(cost, o.cost);
    }

    public State(double cost, int vertex)
    {
        this.cost = cost;
        this.vertex = vertex;
    }
}
