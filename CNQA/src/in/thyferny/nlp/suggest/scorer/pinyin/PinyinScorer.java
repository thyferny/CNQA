
package in.thyferny.nlp.suggest.scorer.pinyin;

import in.thyferny.nlp.suggest.scorer.BaseScorer;


public class PinyinScorer extends BaseScorer<PinyinKey>
{
    @Override
    protected PinyinKey generateKey(String sentence)
    {
        PinyinKey pinyinKey = new PinyinKey(sentence);
        if (pinyinKey.size() == 0) return null;
        return pinyinKey;
    }
}
