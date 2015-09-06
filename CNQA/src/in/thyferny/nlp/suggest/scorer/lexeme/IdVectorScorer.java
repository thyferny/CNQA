
package in.thyferny.nlp.suggest.scorer.lexeme;

import in.thyferny.nlp.suggest.scorer.BaseScorer;


public class IdVectorScorer extends BaseScorer<IdVector>
{
    @Override
    protected IdVector generateKey(String sentence)
    {
        IdVector idVector = new IdVector(sentence);
        if (idVector.idArrayList.size() == 0) return null;
        return idVector;
    }
}
