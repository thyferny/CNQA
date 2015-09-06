
package in.thyferny.nlp.recognition.nt;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.algoritm.Viterbi;
import in.thyferny.nlp.corpus.dictionary.item.EnumItem;
import in.thyferny.nlp.corpus.tag.NT;
import in.thyferny.nlp.corpus.tag.Nature;
import in.thyferny.nlp.dictionary.nt.OrganizationDictionary;
import in.thyferny.nlp.seg.common.Vertex;
import in.thyferny.nlp.seg.common.WordNet;


public class OrganizationRecognition
{
    public static boolean Recognition(List<Vertex> pWordSegResult, WordNet wordNetOptimum, WordNet wordNetAll)
    {
        List<EnumItem<NT>> roleTagList = roleTag(pWordSegResult, wordNetAll);
        if (MyNLP.Config.DEBUG)
        {
            StringBuilder sbLog = new StringBuilder();
            Iterator<Vertex> iterator = pWordSegResult.iterator();
            for (EnumItem<NT> NTEnumItem : roleTagList)
            {
                sbLog.append('[');
                sbLog.append(iterator.next().realWord);
                sbLog.append(' ');
                sbLog.append(NTEnumItem);
                sbLog.append(']');
            }
            System.out.printf("机构名角色观察：%s\n", sbLog.toString());
        }
        List<NT> NTList = viterbiExCompute(roleTagList);
        if (MyNLP.Config.DEBUG)
        {
            StringBuilder sbLog = new StringBuilder();
            Iterator<Vertex> iterator = pWordSegResult.iterator();
            sbLog.append('[');
            for (NT NT : NTList)
            {
                sbLog.append(iterator.next().realWord);
                sbLog.append('/');
                sbLog.append(NT);
                sbLog.append(" ,");
            }
            if (sbLog.length() > 1) sbLog.delete(sbLog.length() - 2, sbLog.length());
            sbLog.append(']');
            System.out.printf("机构名角色标注：%s\n", sbLog.toString());
        }

        OrganizationDictionary.parsePattern(NTList, pWordSegResult, wordNetOptimum, wordNetAll);
        return true;
    }

    public static List<EnumItem<NT>> roleTag(List<Vertex> vertexList, WordNet wordNetAll)
    {
        List<EnumItem<NT>> tagList = new LinkedList<EnumItem<NT>>();
        //        int line = 0;
        for (Vertex vertex : vertexList)
        {
            // 构成更长的
            Nature nature = vertex.guessNature();
            switch (nature)
            {
                case nz:
                {
                    if (vertex.getAttribute().totalFrequency <= 1000)
                    {
                        tagList.add(new EnumItem<NT>(NT.F, 1000));
                    }
                    else break;
                }
                continue;
                case ni:
                case nic:
                case nis:
                case nit:
                {
                    EnumItem<NT> ntEnumItem = new EnumItem<NT>(NT.K, 1000);
                    ntEnumItem.addLabel(NT.D, 1000);
                    tagList.add(ntEnumItem);
                }
                continue;
                case m:
                {
                    EnumItem<NT> ntEnumItem = new EnumItem<NT>(NT.M, 1000);
                    tagList.add(ntEnumItem);
                }
                continue;
            }

            EnumItem<NT> NTEnumItem = OrganizationDictionary.dictionary.get(vertex.word);  // 此处用等效词，更加精准
            if (NTEnumItem == null)
            {
                NTEnumItem = new EnumItem<NT>(NT.Z, OrganizationDictionary.transformMatrixDictionary.getTotalFrequency(NT.Z));
            }
            tagList.add(NTEnumItem);
//            line += vertex.realWord.length();
        }
        return tagList;
    }

    
    public static List<NT> viterbiExCompute(List<EnumItem<NT>> roleTagList)
    {
        return Viterbi.computeEnum(roleTagList, OrganizationDictionary.transformMatrixDictionary);
    }
}
