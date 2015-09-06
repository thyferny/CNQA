
package in.thyferny.nlp.dependency;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.util.LinkedList;
import java.util.List;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.collection.dartsclone.Pair;
import in.thyferny.nlp.corpus.dependency.CoNll.CoNLLSentence;
import in.thyferny.nlp.corpus.io.ByteArray;
import in.thyferny.nlp.dependency.common.Edge;
import in.thyferny.nlp.dependency.common.Node;
import in.thyferny.nlp.model.maxent.MaxEntModel;
import in.thyferny.nlp.seg.common.Term;
import in.thyferny.nlp.tokenizer.NLPTokenizer;
import in.thyferny.nlp.utility.Predefine;


public class MaxEntDependencyParser extends MinimumSpanningTreeParser
{
    static final MaxEntDependencyParser INSTANCE = new MaxEntDependencyParser();
    static MaxEntModel model;
    static
    {
        long start = System.currentTimeMillis();
        ByteArray byteArray = ByteArray.createByteArray(MyNLP.Config.MaxEntModelPath + Predefine.BIN_EXT);
        if (byteArray != null)
        {
            model = MaxEntModel.create(byteArray);
        }
        else
        {
            model = MaxEntModel.create(MyNLP.Config.MaxEntModelPath);
        }
        String result = model == null ? "失败" : "成功";
        logger.info("最大熵依存句法模型载入" + result + "，耗时" + (System.currentTimeMillis() - start) + " ms");
    }

    public static CoNLLSentence compute(List<Term> termList)
    {
        return INSTANCE.parse(termList);
    }

    public static CoNLLSentence compute(String text)
    {
        return compute(NLPTokenizer.segment(text));
    }

    @Override
    protected Edge makeEdge(Node[] nodeArray, int from, int to)
    {
        LinkedList<String> context = new LinkedList<String>();
        int index = from;
        for (int i = index - 2; i < index + 2 + 1; ++i)
        {
            Node w = i >= 0 && i < nodeArray.length ? nodeArray[i] : Node.NULL;
            context.add(w.compiledWord + "i" + (i - index));      // 在尾巴上做个标记，不然特征冲突了
            context.add(w.label + "i" + (i - index));
        }
        index = to;
        for (int i = index - 2; i < index + 2 + 1; ++i)
        {
            Node w = i >= 0 && i < nodeArray.length ? nodeArray[i] : Node.NULL;
            context.add(w.compiledWord + "j" + (i - index));      // 在尾巴上做个标记，不然特征冲突了
            context.add(w.label + "j" + (i - index));
        }
        context.add(nodeArray[from].compiledWord + '→' + nodeArray[to].compiledWord);
        context.add(nodeArray[from].label + '→' + nodeArray[to].label);
        context.add(nodeArray[from].compiledWord + '→' + nodeArray[to].compiledWord + (from - to));
        context.add(nodeArray[from].label + '→' + nodeArray[to].label + (from - to));
        Node wordBeforeI = from - 1 >= 0 ? nodeArray[from - 1] : Node.NULL;
        Node wordBeforeJ = to - 1 >= 0 ? nodeArray[to - 1] : Node.NULL;
        context.add(wordBeforeI.compiledWord + '@' + nodeArray[from].compiledWord + '→' + nodeArray[to].compiledWord);
        context.add(nodeArray[from].compiledWord + '→' + wordBeforeJ.compiledWord + '@' + nodeArray[to].compiledWord);
        context.add(wordBeforeI.label + '@' + nodeArray[from].label + '→' + nodeArray[to].label);
        context.add(nodeArray[from].label + '→' + wordBeforeJ.label + '@' + nodeArray[to].label);
        List<Pair<String, Double>> pairList = model.predict(context.toArray(new String[0]));
        Pair<String, Double> maxPair = new Pair<String, Double>("null", -1.0);
//        System.out.println(context);
//        System.out.println(pairList);
        for (Pair<String, Double> pair : pairList)
        {
            if (pair.getValue() > maxPair.getValue() && !"null".equals(pair.getKey()))
            {
                maxPair = pair;
            }
        }
//        System.out.println(nodeArray[from].word + "→" + nodeArray[to].word + " : " + maxPair);

        return new Edge(from, to, maxPair.getKey(), (float) - Math.log(maxPair.getValue()));
    }
}
