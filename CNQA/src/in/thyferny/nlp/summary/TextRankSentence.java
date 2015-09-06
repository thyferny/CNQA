
package in.thyferny.nlp.summary;


import java.util.*;

import in.thyferny.nlp.dictionary.stopword.CoreStopWordDictionary;
import in.thyferny.nlp.seg.common.Term;
import in.thyferny.nlp.tokenizer.StandardTokenizer;


public class TextRankSentence
{
    
    final static double d = 0.85;
    
    final static int max_iter = 200;
    final static double min_diff = 0.001;
    
    int D;
    
    List<List<String>> docs;
    
    TreeMap<Double, Integer> top;

    
    double[][] weight;
    
    double[] weight_sum;
    
    double[] vertex;

    
    BM25 bm25;

    public TextRankSentence(List<List<String>> docs)
    {
        this.docs = docs;
        bm25 = new BM25(docs);
        D = docs.size();
        weight = new double[D][D];
        weight_sum = new double[D];
        vertex = new double[D];
        top = new TreeMap<Double, Integer>(Collections.reverseOrder());
        solve();
    }

    private void solve()
    {
        int cnt = 0;
        for (List<String> sentence : docs)
        {
            double[] scores = bm25.simAll(sentence);
//            System.out.println(Arrays.toString(scores));
            weight[cnt] = scores;
            weight_sum[cnt] = sum(scores) - scores[cnt]; // 减掉自己，自己跟自己肯定最相似
            vertex[cnt] = 1.0;
            ++cnt;
        }
        for (int iter = 0; iter < max_iter; ++iter)
        {
            double[] m = new double[D];
            double max_diff = 0;
            for (int i = 0; i < D; ++i)
            {
                m[i] = 1 - d;
                for (int j = 0; j < D; ++j)
                {
                    if (j == i || weight_sum[j] == 0) continue;
                    m[i] += (d * weight[j][i] / weight_sum[j] * vertex[j]);
                }
                double diff = Math.abs(m[i] - vertex[i]);
                if (diff > max_diff)
                {
                    max_diff = diff;
                }
            }
            vertex = m;
            if (max_diff <= min_diff) break;
        }
        // 我们来排个序吧
        for (int i = 0; i < D; ++i)
        {
            top.put(vertex[i], i);
        }
    }

    
    public int[] getTopSentence(int size)
    {
        Collection<Integer> values = top.values();
        size = Math.min(size, values.size());
        int[] indexArray = new int[size];
        Iterator<Integer> it = values.iterator();
        for (int i = 0; i < size; ++i)
        {
            indexArray[i] = it.next();
        }
        return indexArray;
    }

    
    private static double sum(double[] array)
    {
        double total = 0;
        for (double v : array)
        {
            total += v;
        }
        return total;
    }

    public static void main(String[] args)
    {
        String document = "算法可大致分为基本算法、数据结构的算法、数论算法、计算几何的算法、图的算法、动态规划以及数值分析、加密算法、排序算法、检索算法、随机化算法、并行算法、厄米变形模型、随机森林算法。\n" +
                "算法可以宽泛的分为三类，\n" +
                "一，有限的确定性算法，这类算法在有限的一段时间内终止。他们可能要花很长时间来执行指定的任务，但仍将在一定的时间内终止。这类算法得出的结果常取决于输入值。\n" +
                "二，有限的非确定算法，这类算法在有限的时间内终止。然而，对于一个（或一些）给定的数值，算法的结果并不是唯一的或确定的。\n" +
                "三，无限的算法，是那些由于没有定义终止定义条件，或定义的条件无法由输入的数据满足而不终止运行的算法。通常，无限算法的产生是由于未能确定的定义终止条件。";
        System.out.println(TextRankSentence.getTopSentenceList(document, 3));
    }

    
    static List<String> spiltSentence(String document)
    {
        List<String> sentences = new ArrayList<String>();
        for (String line : document.split("[\r\n]"))
        {
            line = line.trim();
            if (line.length() == 0) continue;
            for (String sent : line.split("[，,。:：“”？?！!；;]"))
            {
                sent = sent.trim();
                if (sent.length() == 0) continue;
                sentences.add(sent);
            }
        }

        return sentences;
    }

    
    public static List<String> getTopSentenceList(String document, int size)
    {
        List<String> sentenceList = spiltSentence(document);
        List<List<String>> docs = new ArrayList<List<String>>();
        for (String sentence : sentenceList)
        {
            List<Term> termList = StandardTokenizer.segment(sentence.toCharArray());
            List<String> wordList = new LinkedList<String>();
            for (Term term : termList)
            {
                if (CoreStopWordDictionary.shouldInclude(term))
                {
                    wordList.add(term.word);
                }
            }
            docs.add(wordList);
//            System.out.println(wordList);
        }
        TextRankSentence textRank = new TextRankSentence(docs);
        int[] topSentence = textRank.getTopSentence(size);
        List<String> resultList = new LinkedList<String>();
        for (int i : topSentence)
        {
            resultList.add(sentenceList.get(i));
        }
        return resultList;
    }
}
