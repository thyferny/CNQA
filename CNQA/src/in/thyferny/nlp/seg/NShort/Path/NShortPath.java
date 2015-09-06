
package in.thyferny.nlp.seg.NShort.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import in.thyferny.nlp.seg.common.EdgeFrom;
import in.thyferny.nlp.seg.common.Graph;
import in.thyferny.nlp.utility.Predefine;


public class NShortPath
{
    
    private Graph graph;
    
    private int N;
    
    private int vertexCount;
    
    private CQueue[][] fromArray;
    
    private double[][] weightArray;

    
    public NShortPath(Graph graph, int N)
    {
        calculate(graph, N);
    }

    
    private void initNShortPath(Graph inGraph, int nValueKind)
    {
        graph = inGraph;
        N = nValueKind;

        // 获取顶点的数目
        vertexCount = inGraph.vertexes.length;

        fromArray = new CQueue[vertexCount - 1][];  // 不包含起点
        weightArray = new double[vertexCount - 1][];

        //每个节点的最小堆
        for (int i = 0; i < vertexCount - 1; i++)
        {
            fromArray[i] = new CQueue[nValueKind];
            weightArray[i] = new double[nValueKind];

            for (int j = 0; j < nValueKind; j++)
                fromArray[i][j] = new CQueue();
        }
    }

    
    private void calculate(Graph inGraph, int nValueKind)
    {
        initNShortPath(inGraph, nValueKind);

        QueueElement tmpElement;
        CQueue queWork = new CQueue();
        double eWeight;

        for (int nCurNode = 1; nCurNode < vertexCount; ++nCurNode)
        {
            // 将所有到当前结点（nCurNode)可能到达的边根据eWeight排序并压入队列
            enQueueCurNodeEdges(queWork, nCurNode);

            // 初始化当前结点所有边的eWeight值
            for (int i = 0; i < N; ++i)
                weightArray[nCurNode - 1][i] = Double.MAX_VALUE;

            // 将queWork中的内容装入fromArray
            tmpElement = queWork.deQueue();
            if (tmpElement != null)
            {
                for (int i = 0; i < N; ++i)
                {
                    eWeight = tmpElement.weight;
                    weightArray[nCurNode - 1][i] = eWeight;
                    do
                    {
                        fromArray[nCurNode - 1][i].enQueue(new QueueElement(tmpElement.from, tmpElement.index, 0));
                        tmpElement = queWork.deQueue();
                        if (tmpElement == null)
                        {
                            i = N;
                            break;
                        }
                    } while (tmpElement.weight == eWeight);
                }
            }
        }
    }

    
    private void enQueueCurNodeEdges(CQueue queWork, int nCurNode)
    {
        int nPreNode;
        double eWeight;
        List<EdgeFrom> pEdgeToList;

        queWork.clear();
        pEdgeToList = graph.getEdgeListTo(nCurNode);

        // Get all the edgesFrom
        for (EdgeFrom e : pEdgeToList)
        {
            nPreNode = e.from;
            eWeight = e.weight;

            for (int i = 0; i < N; i++)
            {
                // 第一个结点，没有PreNode，直接加入队列
                if (nPreNode == 0)
                {
                    queWork.enQueue(new QueueElement(nPreNode, i, eWeight));
                    break;
                }

                // 如果PreNode的Weight == INFINITE_VALUE，则没有必要继续下去了
                if (weightArray[nPreNode - 1][i] == Double.MAX_VALUE)
                    break;

                queWork.enQueue(new QueueElement(nPreNode, i, eWeight + weightArray[nPreNode - 1][i]));
            }
        }
    }

    
    public List<int[]> getPaths(int index)
    {
        assert (index <= N && index >= 0);

        Stack<PathNode> stack = new Stack<PathNode>();
        int curNode = vertexCount - 1, curIndex = index;
        QueueElement element;
        PathNode node;
        int[] aPath;
        List<int[]> result = new ArrayList<int[]>();

        element = fromArray[curNode - 1][curIndex].GetFirst();
        while (element != null)
        {
            // ---------- 通过压栈得到路径 -----------
            stack.push(new PathNode(curNode, curIndex));
            stack.push(new PathNode(element.from, element.index));
            curNode = element.from;

            while (curNode != 0)
            {
                element = fromArray[element.from - 1][element.index].GetFirst();
//                System.out.println(element.from + " " + element.index);
                stack.push(new PathNode(element.from, element.index));
                curNode = element.from;
            }

            // -------------- 输出路径 --------------
            PathNode[] nArray = new PathNode[stack.size()];
            for (int i = 0; i < stack.size(); ++i)
            {
                nArray[i] = stack.get(stack.size() - i - 1);
            }
            aPath = new int[nArray.length];

            for (int i = 0; i < aPath.length; i++)
                aPath[i] = nArray[i].from;

            result.add(aPath);

            // -------------- 出栈以检查是否还有其它路径 --------------
            do
            {
                node = stack.pop();
                curNode = node.from;
                curIndex = node.index;

            } while (curNode < 1 || (stack.size() != 0 && !fromArray[curNode - 1][curIndex].CanGetNext()));

            element = fromArray[curNode - 1][curIndex].GetNext();
        }

        return result;
    }

    
    public Integer[] getBestPath()
    {
        assert (vertexCount > 2);

        Stack<Integer> stack = new Stack<Integer>();
        int curNode = vertexCount - 1, curIndex = 0;
        QueueElement element;

        element = fromArray[curNode - 1][curIndex].GetFirst();

        stack.push(curNode);
        stack.push(element.from);
        curNode = element.from;

        while (curNode != 0)
        {
            element = fromArray[element.from - 1][element.index].GetFirst();
            stack.push(element.from);
            curNode = element.from;
        }

        return (Integer[]) stack.toArray();
    }


    
    public List<int[]> getNPaths(int n)
    {
        List<int[]> result = new ArrayList<int[]>();

        n = Math.min(Predefine.MAX_SEGMENT_NUM, n);
        for (int i = 0; i < N && result.size() < n; ++i)
        {
            List<int[]> pathList = getPaths(i);
            for (int[] path : pathList)
            {
                if (result.size() == n) break;
                result.add(path);
            }
        }

        return result;
    }

    
    public List<int[]> getNPaths()
    {
        return getNPaths(Predefine.MAX_SEGMENT_NUM);
    }
}
