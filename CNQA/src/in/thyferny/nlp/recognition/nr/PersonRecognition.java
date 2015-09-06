
package in.thyferny.nlp.recognition.nr;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.algoritm.Viterbi;
import in.thyferny.nlp.corpus.dictionary.item.EnumItem;
import in.thyferny.nlp.corpus.tag.NR;
import in.thyferny.nlp.corpus.tag.Nature;
import in.thyferny.nlp.dictionary.nr.PersonDictionary;
import in.thyferny.nlp.seg.common.Vertex;
import in.thyferny.nlp.seg.common.WordNet;


public class PersonRecognition
{
    public static boolean Recognition(List<Vertex> pWordSegResult, WordNet wordNetOptimum, WordNet wordNetAll)
    {
        List<EnumItem<NR>> roleTagList = roleTag(pWordSegResult);
        if (MyNLP.Config.DEBUG)
        {
            StringBuilder sbLog = new StringBuilder();
            Iterator<Vertex> iterator = pWordSegResult.iterator();
            for (EnumItem<NR> nrEnumItem : roleTagList)
            {
                sbLog.append('[');
                sbLog.append(iterator.next().realWord);
                sbLog.append(' ');
                sbLog.append(nrEnumItem);
                sbLog.append(']');
            }
            System.out.printf("人名角色观察：%s\n", sbLog.toString());
        }
        List<NR> nrList = viterbiExCompute(roleTagList);
        if (MyNLP.Config.DEBUG)
        {
            StringBuilder sbLog = new StringBuilder();
            Iterator<Vertex> iterator = pWordSegResult.iterator();
            sbLog.append('[');
            for (NR nr : nrList)
            {
                sbLog.append(iterator.next().realWord);
                sbLog.append('/');
                sbLog.append(nr);
                sbLog.append(" ,");
            }
            if (sbLog.length() > 1) sbLog.delete(sbLog.length() - 2, sbLog.length());
            sbLog.append(']');
            System.out.printf("人名角色标注：%s\n", sbLog.toString());
        }

        PersonDictionary.parsePattern(nrList, pWordSegResult, wordNetOptimum, wordNetAll);
        return true;
    }

    public static List<EnumItem<NR>> roleTag(List<Vertex> pWordSegResult)
    {
        List<EnumItem<NR>> tagList = new LinkedList<EnumItem<NR>>();
        for (Vertex vertex : pWordSegResult)
        {
            // 有些双名实际上可以构成更长的三名
            if (Nature.nr == vertex.getNature() && vertex.getAttribute().totalFrequency <= 1000)
            {
                if (vertex.realWord.length() == 2)
                {
                    tagList.add(new EnumItem<NR>(NR.X, NR.G));
                    continue;
                }
            }
            EnumItem<NR> nrEnumItem = PersonDictionary.dictionary.get(vertex.realWord);
            if (nrEnumItem == null)
            {
                nrEnumItem = new EnumItem<NR>(NR.A, PersonDictionary.transformMatrixDictionary.getTotalFrequency(NR.A));
            }
            tagList.add(nrEnumItem);
        }
        return tagList;
    }

    
    public static List<NR> viterbiCompute(List<EnumItem<NR>> roleTagList)
    {
        List<NR> resultList = new LinkedList<NR>();
        // HMM五元组
        int[] observations = new int[roleTagList.size()];
        for (int i = 0; i < observations.length; ++i)
        {
            observations[i] = i;
        }
        double[][] emission_probability = new double[PersonDictionary.transformMatrixDictionary.ordinaryMax][observations.length];
        for (int i = 0; i < emission_probability.length; ++i)
        {
            for (int j = 0; j < emission_probability[i].length; ++j)
            {
                emission_probability[i][j] = 1e8;
            }
        }
        for (int s : PersonDictionary.transformMatrixDictionary.states)
        {
            Iterator<EnumItem<NR>> iterator = roleTagList.iterator();
            for (int o : observations)
            {
                NR sNR = NR.values()[s];
                EnumItem<NR> item = iterator.next();
                double frequency = item.getFrequency(sNR);
                if (frequency == 0)
                {
                    emission_probability[s][o] = 1e8;
                }
                else
                {
                    emission_probability[s][o] = -Math.log(frequency / PersonDictionary.transformMatrixDictionary.getTotalFrequency(sNR));
                }

            }
        }
        int[] result = Viterbi.compute(observations,
                                        PersonDictionary.transformMatrixDictionary.states,
                                        PersonDictionary.transformMatrixDictionary.start_probability,
                                        PersonDictionary.transformMatrixDictionary.transititon_probability,
                                        emission_probability
        );
        for (int r : result)
        {
            resultList.add(NR.values()[r]);
        }
        return resultList;
    }

    
    public static List<NR> viterbiExCompute(List<EnumItem<NR>> roleTagList)
    {
        return Viterbi.computeEnumSimply(roleTagList, PersonDictionary.transformMatrixDictionary);
    }
}
