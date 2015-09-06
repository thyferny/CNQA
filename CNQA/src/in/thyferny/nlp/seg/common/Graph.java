
package in.thyferny.nlp.seg.common;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class Graph
{
    
    public Vertex[] vertexes;

    
    public List<EdgeFrom>[] edgesTo;

    
    public Graph(Vertex[] vertexes)
    {
        int size = vertexes.length;
        this.vertexes = vertexes;
        edgesTo = new List[size];
        for (int i = 0; i < size; ++i)
        {
            edgesTo[i] = new LinkedList<EdgeFrom>();
        }
    }

    
    public void connect(int from, int to, double weight)
    {
        edgesTo[to].add(new EdgeFrom(from, weight, vertexes[from].word + '@' + vertexes[to].word));
    }


    
    public List<EdgeFrom> getEdgeListTo(int to)
    {
        return edgesTo[to];
    }

    @Override
    public String toString()
    {
        return "Graph{" +
                "vertexes=" + Arrays.toString(vertexes) +
                ", edgesTo=" + Arrays.toString(edgesTo) +
                '}';
    }

    public String printByTo()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("========按终点打印========\n");
        for (int to = 0; to < edgesTo.length; ++to)
        {
            List<EdgeFrom> edgeFromList = edgesTo[to];
            for (EdgeFrom edgeFrom : edgeFromList)
            {
                sb.append(String.format("to:%3d, from:%3d, weight:%05.2f, word:%s\n", to, edgeFrom.from, edgeFrom.weight, edgeFrom.name));
            }
        }

        return sb.toString();
    }

    
    public List<Vertex> parsePath(int[] path)
    {
        List<Vertex> vertexList = new LinkedList<Vertex>();
        for (int i : path)
        {
            vertexList.add(vertexes[i]);
        }

        return vertexList;
    }

    
    public static String parseResult(List<Vertex> path)
    {
        if (path.size() < 2)
        {
            throw new RuntimeException("路径节点数小于2:" + path);
        }
        StringBuffer sb = new StringBuffer();

        for (int i = 1; i < path.size() - 1; ++i)
        {
            Vertex v = path.get(i);
            sb.append(v.getRealWord() + " ");
        }

        return sb.toString();
    }

    public Vertex[] getVertexes()
    {
        return vertexes;
    }

    public List<EdgeFrom>[] getEdgesTo()
    {
        return edgesTo;
    }
}
