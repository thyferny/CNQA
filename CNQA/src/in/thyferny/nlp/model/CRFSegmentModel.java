
package in.thyferny.nlp.model;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.util.LinkedList;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.collection.trie.DoubleArrayTrie;
import in.thyferny.nlp.collection.trie.ITrie;
import in.thyferny.nlp.collection.trie.bintrie.BinTrie;
import in.thyferny.nlp.model.crf.CRFModel;
import in.thyferny.nlp.model.crf.FeatureFunction;
import in.thyferny.nlp.model.crf.Table;


public class CRFSegmentModel extends CRFModel
{
    public static CRFModel crfModel;

    static
    {
        logger.info("CRF分词模型正在加载 " + MyNLP.Config.CRFSegmentModelPath);
        long start = System.currentTimeMillis();
        crfModel = CRFModel.loadTxt(MyNLP.Config.CRFSegmentModelPath, new CRFSegmentModel(new BinTrie<FeatureFunction>()));
        if (crfModel == null)
        {
            logger.severe("CRF分词模型加载 " + MyNLP.Config.CRFSegmentModelPath + " 失败，耗时 " + (System.currentTimeMillis() - start) + " ms");
            System.exit(-1);
        }
        else
            logger.info("CRF分词模型加载 " + MyNLP.Config.CRFSegmentModelPath + " 成功，耗时 " + (System.currentTimeMillis() - start) + " ms");
    }

    private final static int idM = crfModel.getTagId("M");
    private final static int idE = crfModel.getTagId("E");
    private final static int idS = crfModel.getTagId("S");

    
    public CRFSegmentModel(ITrie<FeatureFunction> featureFunctionTrie)
    {
        super(featureFunctionTrie);
    }

    @Override
    public void tag(Table table)
    {
        int size = table.size();
        if (size == 1)
        {
            table.setLast(0, "S");
            return;
        }
        double[][] net = new double[size][4];
        for (int i = 0; i < size; ++i)
        {
            LinkedList<double[]> scoreList = computeScoreList(table, i);
            for (int tag = 0; tag < 4; ++tag)
            {
                net[i][tag] = computeScore(scoreList, tag);
            }
        }
        net[0][idM] = -1000.0;  // 第一个字不可能是M或E
        net[0][idE] = -1000.0;
        int[][] from = new int[size][4];
        for (int i = 1; i < size; ++i)
        {
            for (int now = 0; now < 4; ++now)
            {
                double maxScore = -1e10;
                for (int pre = 0; pre < 4; ++pre)
                {
                    if (matrix[pre][now] <= 0) continue;
                    double score = net[i - 1][pre] + matrix[pre][now] + net[i][now];
                    if (score > maxScore)
                    {
                        maxScore = score;
                        from[i][now] = pre;
                    }
                }
                net[i][now] = maxScore;
            }
        }
        // 反向回溯最佳路径
        int maxTag = net[size - 1][idS] > net[size - 1][idE] ? idS : idE;
        table.setLast(size - 1, id2tag[maxTag]);
        maxTag = from[size - 1][maxTag];
        for (int i = size - 2; i > 0; --i)
        {
            table.setLast(i, id2tag[maxTag]);
            maxTag = from[i][maxTag];
        }
        table.setLast(0, id2tag[maxTag]);
    }
}
