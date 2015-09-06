
package in.thyferny.nlp.seg.HMM;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.util.LinkedList;
import java.util.List;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.corpus.io.ByteArray;
import in.thyferny.nlp.model.trigram.CharacterBasedGenerativeModel;
import in.thyferny.nlp.seg.CharacterBasedGenerativeModelSegment;
import in.thyferny.nlp.seg.common.Term;
import in.thyferny.nlp.utility.TextUtility;


public class HMMSegment extends CharacterBasedGenerativeModelSegment
{
    static CharacterBasedGenerativeModel model;
    static
    {
        model = new CharacterBasedGenerativeModel();
        long start = System.currentTimeMillis();
        logger.info("开始从[ " + MyNLP.Config.HMMSegmentModelPath + " ]加载2阶HMM模型");
        try
        {
            ByteArray byteArray = ByteArray.createByteArray(MyNLP.Config.HMMSegmentModelPath);
            if (byteArray == null)
            {
                logger.severe("HMM分词模型[ " + MyNLP.Config.HMMSegmentModelPath + " ]不存在" );
                System.exit(-1);
            }
            model.load(byteArray);
        }
        catch (Exception e)
        {
            logger.severe("发生了异常：" + TextUtility.exceptionToString(e));
            System.exit(-1);
        }
        logger.info("加载成功，耗时：" + (System.currentTimeMillis() - start) + " ms");
    }

    @Override
    protected List<Term> segSentence(char[] sentence)
    {
        char[] tag = model.tag(sentence);
        List<Term> termList = new LinkedList<Term>();
        int offset = 0;
        for (int i = 0; i < tag.length; offset += 1, ++i)
        {
            switch (tag[i])
            {
                case 'b':
                {
                    int begin = offset;
                    while (tag[i] != 'e')
                    {
                        offset += 1;
                        ++i;
                        if (i == tag.length)
                        {
                            break;
                        }
                    }
                    if (i == tag.length)
                    {
                        termList.add(new Term(new String(sentence, begin, offset - begin), null));
                    }
                    else
                        termList.add(new Term(new String(sentence, begin, offset - begin + 1), null));
                }
                break;
                default:
                {
                    termList.add(new Term(new String(sentence, offset, 1), null));
                }
                break;
            }
        }

        return termList;
    }
}
