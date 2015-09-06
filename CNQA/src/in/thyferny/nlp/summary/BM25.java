
package in.thyferny.nlp.summary;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class BM25
{
    
    int D;

    
    double avgdl;

    
    List<List<String>> docs;

    
    Map<String, Integer>[] f;

    
    Map<String, Integer> df;

    
    Map<String, Double> idf;

    
    final static float k1 = 1.5f;

    
    final static float b = 0.75f;

    public BM25(List<List<String>> docs)
    {
        this.docs = docs;
        D = docs.size();
        for (List<String> sentence : docs)
        {
            avgdl += sentence.size();
        }
        avgdl /= D;
        f = new Map[D];
        df = new TreeMap<String, Integer>();
        idf = new TreeMap<String, Double>();
        init();
    }

    
    private void init()
    {
        int index = 0;
        for (List<String> sentence : docs)
        {
            Map<String, Integer> tf = new TreeMap<String, Integer>();
            for (String word : sentence)
            {
                Integer freq = tf.get(word);
                freq = (freq == null ? 0 : freq) + 1;
                tf.put(word, freq);
            }
            f[index] = tf;
            for (Map.Entry<String, Integer> entry : tf.entrySet())
            {
                String word = entry.getKey();
                Integer freq = df.get(word);
                freq = (freq == null ? 0 : freq) + 1;
                df.put(word, freq);
            }
            ++index;
        }
        for (Map.Entry<String, Integer> entry : df.entrySet())
        {
            String word = entry.getKey();
            Integer freq = entry.getValue();
            idf.put(word, Math.log(D - freq + 0.5) - Math.log(freq + 0.5));
        }
    }

    public double sim(List<String> sentence, int index)
    {
        double score = 0;
        for (String word : sentence)
        {
            if (!f[index].containsKey(word)) continue;
            int d = docs.get(index).size();
            Integer wf = f[index].get(word);
            score += (idf.get(word) * wf * (k1 + 1)
                    / (wf + k1 * (1 - b + b * d
                                                / avgdl)));
        }

        return score;
    }

    public double[] simAll(List<String> sentence)
    {
        double[] scores = new double[D];
        for (int i = 0; i < D; ++i)
        {
            scores[i] = sim(sentence, i);
        }
        return scores;
    }
}
