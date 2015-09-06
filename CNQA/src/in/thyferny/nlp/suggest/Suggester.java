
package in.thyferny.nlp.suggest;


import java.util.*;

import in.thyferny.nlp.suggest.scorer.BaseScorer;
import in.thyferny.nlp.suggest.scorer.IScorer;
import in.thyferny.nlp.suggest.scorer.editdistance.EditDistanceScorer;
import in.thyferny.nlp.suggest.scorer.lexeme.IdVector;
import in.thyferny.nlp.suggest.scorer.lexeme.IdVectorScorer;
import in.thyferny.nlp.suggest.scorer.pinyin.PinyinScorer;


public class Suggester implements ISuggester
{
    List<BaseScorer> scorerList;

    public Suggester()
    {
        scorerList = new ArrayList<BaseScorer>();
        scorerList.add(new IdVectorScorer());
        scorerList.add(new EditDistanceScorer());
        scorerList.add(new PinyinScorer());
    }

    public Suggester(List<BaseScorer> scorerList)
    {
        this.scorerList = scorerList;
    }

    
    public Suggester(BaseScorer... scorers)
    {
        scorerList = new ArrayList<BaseScorer>(scorers.length);
        for (BaseScorer scorer : scorers)
        {
            scorerList.add(scorer);
        }
    }

    @Override
    public void addSentence(String sentence)
    {
        for (IScorer scorer : scorerList)
        {
            scorer.addSentence(sentence);
        }
    }

    @Override
    public List<String> suggest(String key, int size)
    {
        List<String> resultList = new ArrayList<String>(size);
        TreeMap<String, Double> scoreMap = new TreeMap<String, Double>();
        for (BaseScorer scorer : scorerList)
        {
            Map<String, Double> map = scorer.computeScore(key);
            Double max = max(map);  // 用于正规化一个map
            for (Map.Entry<String, Double> entry : map.entrySet())
            {
                Double score = scoreMap.get(entry.getKey());
                if (score == null) score = 0.0;
                scoreMap.put(entry.getKey(), score / max + entry.getValue() * scorer.boost);
            }
        }
        for (Map.Entry<Double, Set<String>> entry : sortScoreMap(scoreMap).entrySet())
        {
            for (String sentence : entry.getValue())
            {
                if (resultList.size() >= size) return resultList;
                resultList.add(sentence);
            }
        }

        return resultList;
    }

    
    private static TreeMap<Double ,Set<String>> sortScoreMap(TreeMap<String, Double> scoreMap)
    {
        TreeMap<Double, Set<String>> result = new TreeMap<Double, Set<String>>(Collections.reverseOrder());
        for (Map.Entry<String, Double> entry : scoreMap.entrySet())
        {
            Set<String> sentenceSet = result.get(entry.getValue());
            if (sentenceSet == null)
            {
                sentenceSet = new HashSet<String>();
                result.put(entry.getValue(), sentenceSet);
            }
            sentenceSet.add(entry.getKey());
        }

        return result;
    }

    
    private static Double max(Map<String, Double> map)
    {
        Double theMax = 0.0;
        for (Double v : map.values())
        {
            theMax = Math.max(theMax, v);
        }

        return theMax;
    }
}
