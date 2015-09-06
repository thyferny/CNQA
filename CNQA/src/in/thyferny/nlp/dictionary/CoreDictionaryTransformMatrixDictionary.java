
package in.thyferny.nlp.dictionary;

import static in.thyferny.nlp.utility.Predefine.logger;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.corpus.tag.Nature;


public class CoreDictionaryTransformMatrixDictionary
{
    public static TransformMatrixDictionary<Nature> transformMatrixDictionary;
    static
    {
        transformMatrixDictionary = new TransformMatrixDictionary<Nature>(Nature.class);
        long start = System.currentTimeMillis();
        if (!transformMatrixDictionary.load(MyNLP.Config.CoreDictionaryTransformMatrixDictionaryPath))
        {
            System.err.println("加载核心词典词性转移矩阵" + MyNLP.Config.CoreDictionaryTransformMatrixDictionaryPath + "失败");
            System.exit(-1);
        }
        else
        {
            logger.info("加载核心词典词性转移矩阵" + MyNLP.Config.CoreDictionaryTransformMatrixDictionaryPath + "成功，耗时：" + (System.currentTimeMillis() - start) + " ms");
        }
    }
}
