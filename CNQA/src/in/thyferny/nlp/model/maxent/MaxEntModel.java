
package in.thyferny.nlp.model.maxent;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.*;
import java.util.*;

import in.thyferny.nlp.collection.dartsclone.Pair;
import in.thyferny.nlp.collection.trie.DoubleArrayTrie;
import in.thyferny.nlp.corpus.io.ByteArray;
import in.thyferny.nlp.utility.Predefine;
import in.thyferny.nlp.utility.TextUtility;


public class MaxEntModel
{
    
    int correctionConstant;
    
    double correctionParam;
    
    UniformPrior prior;
    
    protected String[] outcomeNames;
    
    EvalParameters evalParams;

    
    DoubleArrayTrie<Integer> pmap;

    
    public final double[] eval(String[] context)
    {
        return (eval(context, new double[evalParams.getNumOutcomes()]));
    }

    
    public final List<Pair<String, Double>> predict(String[] context)
    {
        List<Pair<String, Double>> result = new ArrayList<Pair<String, Double>>(outcomeNames.length);
        double[] p = eval(context);
        for (int i = 0; i < p.length; ++i)
        {
            result.add(new Pair<String, Double>(outcomeNames[i], p[i]));
        }
        return result;
    }

    
    public final List<Pair<String, Double>> predict(Collection<String> context)
    {
        return predict(context.toArray(new String[0]));
    }

    
    public final double[] eval(String[] context, double[] outsums)
    {
        int[] scontexts = new int[context.length];
        for (int i = 0; i < context.length; i++)
        {
            Integer ci = pmap.get(context[i]);
            scontexts[i] = ci == null ? -1 : ci;
        }
        prior.logPrior(outsums);
        return eval(scontexts, outsums, evalParams);
    }

    
    public static double[] eval(int[] context, double[] prior, EvalParameters model)
    {
        Context[] params = model.getParams();
        int numfeats[] = new int[model.getNumOutcomes()];
        int[] activeOutcomes;
        double[] activeParameters;
        double value = 1;
        for (int ci = 0; ci < context.length; ci++)
        {
            if (context[ci] >= 0)
            {
                Context predParams = params[context[ci]];
                activeOutcomes = predParams.getOutcomes();
                activeParameters = predParams.getParameters();
                for (int ai = 0; ai < activeOutcomes.length; ai++)
                {
                    int oid = activeOutcomes[ai];
                    numfeats[oid]++;
                    prior[oid] += activeParameters[ai] * value;
                }
            }
        }

        double normal = 0.0;
        for (int oid = 0; oid < model.getNumOutcomes(); oid++)
        {
            if (model.getCorrectionParam() != 0)
            {
                prior[oid] = Math
                        .exp(prior[oid]
                                     * model.getConstantInverse()
                                     + ((1.0 - ((double) numfeats[oid] / model
                                .getCorrectionConstant())) * model.getCorrectionParam()));
            }
            else
            {
                prior[oid] = Math.exp(prior[oid] * model.getConstantInverse());
            }
            normal += prior[oid];
        }

        for (int oid = 0; oid < model.getNumOutcomes(); oid++)
        {
            prior[oid] /= normal;
        }
        return prior;
    }

    
    public static MaxEntModel create(String path)
    {
        MaxEntModel m = new MaxEntModel();
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            DataOutputStream out = new DataOutputStream(new FileOutputStream(path + Predefine.BIN_EXT));
            br.readLine();  // type
            m.correctionConstant = Integer.parseInt(br.readLine());  // correctionConstant
            out.writeInt(m.correctionConstant);
            m.correctionParam = Double.parseDouble(br.readLine());  // getCorrectionParameter
            out.writeDouble(m.correctionParam);
            // label
            int numOutcomes = Integer.parseInt(br.readLine());
            out.writeInt(numOutcomes);
            String[] outcomeLabels = new String[numOutcomes];
            m.outcomeNames = outcomeLabels;
            for (int i = 0; i < numOutcomes; i++)
            {
                outcomeLabels[i] = br.readLine();
                TextUtility.writeString(outcomeLabels[i], out);
            }
            // pattern
            int numOCTypes = Integer.parseInt(br.readLine());
            out.writeInt(numOCTypes);
            int[][] outcomePatterns = new int[numOCTypes][];
            for (int i = 0; i < numOCTypes; i++)
            {
                StringTokenizer tok = new StringTokenizer(br.readLine(), " ");
                int[] infoInts = new int[tok.countTokens()];
                out.writeInt(infoInts.length);
                for (int j = 0; tok.hasMoreTokens(); j++)
                {
                    infoInts[j] = Integer.parseInt(tok.nextToken());
                    out.writeInt(infoInts[j]);
                }
                outcomePatterns[i] = infoInts;
            }
            // feature
            int NUM_PREDS = Integer.parseInt(br.readLine());
            out.writeInt(NUM_PREDS);
            String[] predLabels = new String[NUM_PREDS];
            m.pmap = new DoubleArrayTrie<Integer>();
            TreeMap<String, Integer> tmpMap = new TreeMap<String, Integer>();
            for (int i = 0; i < NUM_PREDS; i++)
            {
                predLabels[i] = br.readLine();
                TextUtility.writeString(predLabels[i], out);
                tmpMap.put(predLabels[i], i);
            }
            m.pmap.build(tmpMap);
            for (Map.Entry<String, Integer> entry : tmpMap.entrySet())
            {
                out.writeInt(entry.getValue());
            }
            m.pmap.save(out);
            // params
            Context[] params = new Context[NUM_PREDS];
            int pid = 0;
            for (int i = 0; i < outcomePatterns.length; i++)
            {
                int[] outcomePattern = new int[outcomePatterns[i].length - 1];
                for (int k = 1; k < outcomePatterns[i].length; k++)
                {
                    outcomePattern[k - 1] = outcomePatterns[i][k];
                }
                for (int j = 0; j < outcomePatterns[i][0]; j++)
                {
                    double[] contextParameters = new double[outcomePatterns[i].length - 1];
                    for (int k = 1; k < outcomePatterns[i].length; k++)
                    {
                        contextParameters[k - 1] = Double.parseDouble(br.readLine());
                        out.writeDouble(contextParameters[k - 1]);
                    }
                    params[pid] = new Context(outcomePattern, contextParameters);
                    pid++;
                }
            }
            // prior
            m.prior = new UniformPrior();
            m.prior.setLabels(outcomeLabels);
            // eval
            m.evalParams = new EvalParameters(params, m.correctionParam, m.correctionConstant, outcomeLabels.length);
        }
        catch (Exception e)
        {
            logger.severe("从" + path + "加载最大熵模型失败！" + TextUtility.exceptionToString(e));
            return null;
        }
        return m;
    }

    
    public static MaxEntModel create(ByteArray byteArray)
    {
        MaxEntModel m = new MaxEntModel();
        m.correctionConstant = byteArray.nextInt();  // correctionConstant
        m.correctionParam = byteArray.nextDouble();  // getCorrectionParameter
        // label
        int numOutcomes = byteArray.nextInt();
        String[] outcomeLabels = new String[numOutcomes];
        m.outcomeNames = outcomeLabels;
        for (int i = 0; i < numOutcomes; i++) outcomeLabels[i] = byteArray.nextString();
        // pattern
        int numOCTypes = byteArray.nextInt();
        int[][] outcomePatterns = new int[numOCTypes][];
        for (int i = 0; i < numOCTypes; i++)
        {
            int length = byteArray.nextInt();
            int[] infoInts = new int[length];
            for (int j = 0; j < length; j++)
            {
                infoInts[j] = byteArray.nextInt();
            }
            outcomePatterns[i] = infoInts;
        }
        // feature
        int NUM_PREDS = byteArray.nextInt();
        String[] predLabels = new String[NUM_PREDS];
        m.pmap = new DoubleArrayTrie<Integer>();
        for (int i = 0; i < NUM_PREDS; i++)
        {
            predLabels[i] = byteArray.nextString();
        }
        Integer[] v = new Integer[NUM_PREDS];
        for (int i = 0; i < v.length; i++)
        {
            v[i] = byteArray.nextInt();
        }
        m.pmap.load(byteArray, v);
        // params
        Context[] params = new Context[NUM_PREDS];
        int pid = 0;
        for (int i = 0; i < outcomePatterns.length; i++)
        {
            int[] outcomePattern = new int[outcomePatterns[i].length - 1];
            for (int k = 1; k < outcomePatterns[i].length; k++)
            {
                outcomePattern[k - 1] = outcomePatterns[i][k];
            }
            for (int j = 0; j < outcomePatterns[i][0]; j++)
            {
                double[] contextParameters = new double[outcomePatterns[i].length - 1];
                for (int k = 1; k < outcomePatterns[i].length; k++)
                {
                    contextParameters[k - 1] = byteArray.nextDouble();
                }
                params[pid] = new Context(outcomePattern, contextParameters);
                pid++;
            }
        }
        // prior
        m.prior = new UniformPrior();
        m.prior.setLabels(outcomeLabels);
        // eval
        m.evalParams = new EvalParameters(params, m.correctionParam, m.correctionConstant, outcomeLabels.length);
        return m;
    }
}
