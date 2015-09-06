
package in.thyferny.nlp.utility;

import static in.thyferny.nlp.utility.Predefine.*;

import in.thyferny.nlp.dictionary.CoreBiGramTableDictionary;
import in.thyferny.nlp.seg.common.Vertex;


public class MathTools
{
    
    public static double calculateWeight(Vertex from, Vertex to)
    {
        int frequency = from.getAttribute().totalFrequency;
        if (frequency == 0)
        {
            frequency = 1;  // 防止发生除零错误
        }
//        int nTwoWordsFreq = BiGramDictionary.getBiFrequency(from.word, to.word);
        int nTwoWordsFreq = CoreBiGramTableDictionary.getBiFrequency(from.wordID, to.wordID);
        double value = -Math.log(dSmoothingPara * frequency / (MAX_FREQUENCY) + (1 - dSmoothingPara) * ((1 - dTemp) * nTwoWordsFreq / frequency + dTemp));
        if (value < 0.0)
        {
            value = -value;
        }
//        logger.info(String.format("%5s frequency:%6d, %s nTwoWordsFreq:%3d, weight:%.2f", from.word, frequency, from.word + "@" + to.word, nTwoWordsFreq, value));
        return value;
    }
}
