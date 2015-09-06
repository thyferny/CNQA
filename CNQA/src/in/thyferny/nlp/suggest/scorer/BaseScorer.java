
package in.thyferny.nlp.suggest.scorer;

import java.util.*;



public abstract class BaseScorer<T extends ISentenceKey> implements IScorer
{
    public BaseScorer()
    {
        storage = new TreeMap<T, Set<String>>();
    }

    
    protected Map<T, Set<String>> storage;
    
    public double boost = 1.0;

    
    public BaseScorer setBoost(double boost)
    {
        this.boost = boost;
        return this;
    }

    @Override
    public void addSentence(String sentence)
    {
        T key = generateKey(sentence);
        if (key == null) return;
        Set<String> set = storage.get(key);
        if (set == null)
        {
            set = new TreeSet<String>();
            storage.put(key, set);
        }
        set.add(sentence);
    }

    
    protected abstract T generateKey(String sentence);

    @Override
    public Map<String, Double> computeScore(String outerSentence)
    {
        TreeMap<String, Double> result = new TreeMap<String, Double>(Collections.reverseOrder());
        T keyOuter = generateKey(outerSentence);
        if (keyOuter == null) return result;
        for (Map.Entry<T, Set<String>> entry : storage.entrySet())
        {
            T key = entry.getKey();
            Double score = keyOuter.similarity(key);
            for (String sentence : entry.getValue())
            {
                result.put(sentence, score);
            }
        }
        return result;
    }
}
