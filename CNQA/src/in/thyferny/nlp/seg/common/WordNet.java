
package in.thyferny.nlp.seg.common;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import in.thyferny.nlp.corpus.tag.Nature;
import in.thyferny.nlp.dictionary.CoreDictionary;
import in.thyferny.nlp.seg.NShort.Path.AtomNode;
import in.thyferny.nlp.utility.MathTools;
import in.thyferny.nlp.utility.Predefine;


public class WordNet
{
    
    private LinkedList<Vertex> vertexes[];

    
    int size;

    
    public String sentence;

    
    public char[] charArray;

    
    public WordNet(String sentence)
    {
        this(sentence.toCharArray());
    }

    public WordNet(char[] charArray)
    {
        this.charArray = charArray;
        vertexes = new LinkedList[charArray.length + 2];
        for (int i = 0; i < vertexes.length; ++i)
        {
            vertexes[i] = new LinkedList<Vertex>();
        }
        vertexes[0].add(Vertex.newB());
        vertexes[vertexes.length - 1].add(Vertex.newE());
        size = 2;
    }

    public WordNet(char[] charArray, List<Vertex> vertexList)
    {
        this.charArray = charArray;
        vertexes = new LinkedList[charArray.length + 2];
        for (int i = 0; i < vertexes.length; ++i)
        {
            vertexes[i] = new LinkedList<Vertex>();
        }
        int i = 0;
        for (Vertex vertex : vertexList)
        {
            vertexes[i].add(vertex);
            ++size;
            i += vertex.realWord.length();
        }
    }

    
    public void add(int line, Vertex vertex)
    {
        for (Vertex oldVertex : vertexes[line])
        {
            // 保证唯一性
            if (oldVertex.realWord.length() == vertex.realWord.length()) return;
        }
        vertexes[line].add(vertex);
        ++size;
    }

    
    public void push(int line, Vertex vertex)
    {
        Iterator<Vertex> iterator = vertexes[line].iterator();
        while (iterator.hasNext())
        {
            if (iterator.next().realWord.length() == vertex.realWord.length())
            {
                iterator.remove();
                --size;
                break;
            }
        }
        vertexes[line].add(vertex);
        ++size;
    }

    
    public void insert(int line, Vertex vertex, WordNet wordNetAll)
    {
        for (Vertex oldVertex : vertexes[line])
        {
            // 保证唯一性
            if (oldVertex.realWord.length() == vertex.realWord.length()) return;
        }
        vertexes[line].add(vertex);
        ++size;
        // 保证连接
        for (int l = line - 1; l > 1; --l)
        {
            if (get(l, 1) == null)
            {
                Vertex first = wordNetAll.getFirst(l);
                if (first == null) break;
                vertexes[l].add(first);
                ++size;
                if (vertexes[l].size() > 1) break;
            }
            else
            {
                break;
            }
        }
        // 首先保证这个词语可直达
        int l = line + vertex.realWord.length();
        if (get(l).size() == 0)
        {
            List<Vertex> targetLine = wordNetAll.get(l);
            if (targetLine == null || targetLine.size() == 0) return;
            vertexes[l].addAll(targetLine);
            size += targetLine.size();
        }
        // 直达之后一直往后
        for (++l; l < vertexes.length; ++l)
        {
            if (get(l).size() == 0)
            {
                Vertex first = wordNetAll.getFirst(l);
                if (first == null) break;
                vertexes[l].add(first);
                ++size;
                if (vertexes[l].size() > 1) break;
            }
            else
            {
                break;
            }
        }
    }

    
    public void addAll(List<Vertex> vertexList)
    {
        int i = 0;
        for (Vertex vertex : vertexList)
        {
            add(i, vertex);
            i += vertex.realWord.length();
        }
    }

    
    public List<Vertex> get(int line)
    {
        return vertexes[line];
    }

    
    public Vertex getFirst(int line)
    {
        Iterator<Vertex> iterator = vertexes[line].iterator();
        if (iterator.hasNext()) return iterator.next();

        return null;
    }

    
    public Vertex get(int line, int length)
    {
        for (Vertex vertex : vertexes[line])
        {
            if (vertex.realWord.length() == length)
            {
                return vertex;
            }
        }

        return null;
    }

    
    public void add(int line, List<AtomNode> atomSegment)
    {
        // 将原子部分存入m_segGraph
        int offset = 0;
        for (AtomNode atomNode : atomSegment)//Init the cost array
        {
            String sWord = atomNode.sWord;//init the word
            Nature nature = Nature.n;
            switch (atomNode.nPOS)
            {
                case Predefine.CT_CHINESE:
                    break;
                case Predefine.CT_INDEX:
                case Predefine.CT_NUM:
                    nature = Nature.m;
                    sWord = "未##数";
                    break;
                case Predefine.CT_DELIMITER:
                    nature = Nature.w;
                    break;
                case Predefine.CT_LETTER:
                    nature = Nature.nx;
                    sWord = "未##串";
                    break;
                case Predefine.CT_SINGLE://12021-2129-3121
                    nature = Nature.nx;
                    sWord = "未##串";
                    break;
                default:
                    break;
            }
            add(line + offset, new Vertex(sWord, atomNode.sWord, new CoreDictionary.Attribute(nature, 1)));
            offset += atomNode.sWord.length();
        }
    }

    public int size()
    {
        return size;
    }

    
    private Vertex[] getVertexesLineFirst()
    {
        Vertex[] vertexes = new Vertex[size];
        int i = 0;
        for (List<Vertex> vertexList : this.vertexes)
        {
            for (Vertex v : vertexList)
            {
                v.index = i;    // 设置id
                vertexes[i++] = v;
            }
        }

        return vertexes;
    }

    
    public Graph toGraph()
    {
        Graph graph = new Graph(getVertexesLineFirst());

        for (int row = 0; row < vertexes.length - 1; ++row)
        {
            List<Vertex> vertexListFrom = vertexes[row];
            for (Vertex from : vertexListFrom)
            {
                assert from.realWord.length() > 0 : "空节点会导致死循环！";
                int toIndex = row + from.realWord.length();
                for (Vertex to : vertexes[toIndex])
                {
                    graph.connect(from.index, to.index, MathTools.calculateWeight(from, to));
                }
            }
        }
        return graph;
    }

    @Override
    public String toString()
    {
//        return "Graph{" +
//                "vertexes=" + Arrays.toString(vertexes) +
//                '}';
        StringBuilder sb = new StringBuilder();
        int line = 0;
        for (List<Vertex> vertexList : vertexes)
        {
            sb.append(String.valueOf(line++) + ':' + vertexList.toString()).append("\n");
        }
        return sb.toString();
    }

    
    public void mergeContinuousNsIntoOne()
    {
        for (int row = 0; row < vertexes.length - 1; ++row)
        {
            List<Vertex> vertexListFrom = vertexes[row];
            ListIterator<Vertex> listIteratorFrom = vertexListFrom.listIterator();
            while (listIteratorFrom.hasNext())
            {
                Vertex from = listIteratorFrom.next();
                if (from.getNature() == Nature.ns)
                {
                    int toIndex = row + from.realWord.length();
                    ListIterator<Vertex> listIteratorTo = vertexes[toIndex].listIterator();
                    while (listIteratorTo.hasNext())
                    {
                        Vertex to = listIteratorTo.next();
                        if (to.getNature() == Nature.ns)
                        {
                            // 我们不能直接改，因为很多条线路在公用指针
//                            from.realWord += to.realWord;
                            logger.info("合并【" + from.realWord + "】和【" + to.realWord + "】");
                            listIteratorFrom.set(Vertex.newAddressInstance(from.realWord + to.realWord));
//                            listIteratorTo.remove();
                            break;
                        }
                    }
                }
            }
        }
    }

    
    public void clear()
    {
        for (List<Vertex> vertexList : vertexes)
        {
            vertexList.clear();
        }
        size = 0;
    }

    
    public LinkedList<Vertex>[] getVertexes()
    {
        return vertexes;
    }
}
