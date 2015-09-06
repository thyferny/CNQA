
package in.thyferny.nlp.seg.common;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.util.Map;

import in.thyferny.nlp.corpus.tag.Nature;
import in.thyferny.nlp.dictionary.CoreDictionary;
import in.thyferny.nlp.utility.MathTools;
import in.thyferny.nlp.utility.Predefine;


public class Vertex
{
    
    public String word;
    
    public String realWord;
    
    public CoreDictionary.Attribute attribute;
    
    public int wordID;

    
    public int index;

    
    public static Vertex B = new Vertex(Predefine.TAG_BIGIN, " ", new CoreDictionary.Attribute(Nature.begin, Predefine.MAX_FREQUENCY / 10), CoreDictionary.getWordID(Predefine.TAG_BIGIN));
    
    public static Vertex E = new Vertex(Predefine.TAG_END, " ", new CoreDictionary.Attribute(Nature.begin, Predefine.MAX_FREQUENCY / 10), CoreDictionary.getWordID(Predefine.TAG_END));

    ////////在最短路相关计算中用到的几个变量，之所以放在这里，是为了避免再去生成对象，浪费时间////////
    
    public Vertex from;
    
    public double weight;

    public void updateFrom(Vertex from)
    {
        double weight = from.weight + MathTools.calculateWeight(from, this);
        if (this.from == null || this.weight > weight)
        {
            this.from = from;
            this.weight = weight;
        }
    }

    
    public Vertex(String word, String realWord, CoreDictionary.Attribute attribute)
    {
        this(word, realWord, attribute, -1);
    }

    public Vertex(String word, String realWord, CoreDictionary.Attribute attribute, int wordID)
    {
        if (attribute == null) attribute = new CoreDictionary.Attribute(Nature.n, 1);   // 安全起见
        this.wordID = wordID;
        if (word == null) word = compileRealWord(realWord, attribute);
        assert realWord.length() > 0 : "构造空白节点会导致死循环！";
        this.word = word;
        this.realWord = realWord;
        this.attribute = attribute;
    }

    
    private String compileRealWord(String realWord, CoreDictionary.Attribute attribute)
    {
        if (attribute.nature.length == 1)
        {
            switch (attribute.nature[0])
            {
                case nr:
                case nr1:
                case nr2:
                case nrf:
                case nrj:
                {
                    wordID = CoreDictionary.NR_WORD_ID;
                    return Predefine.TAG_PEOPLE;
                }
                case ns:
                case nsf:
                {
                    wordID = CoreDictionary.NS_WORD_ID;
                    return Predefine.TAG_PLACE;
                }
//                case nz:
                case nx:
                {
                    wordID = CoreDictionary.NX_WORD_ID;
                    return Predefine.TAG_PROPER;
                }
                case nt:
                case ntc:
                case ntcf:
                case ntcb:
                case ntch:
                case nto:
                case ntu:
                case nts:
                case nth:
                case nit:
                {
                    wordID = CoreDictionary.NT_WORD_ID;
                    return Predefine.TAG_GROUP;
                }
                case m:
                case mq:
                {
                    wordID = CoreDictionary.M_WORD_ID;
                    return Predefine.TAG_NUMBER;
                }
                case x:
                {
                    wordID = CoreDictionary.X_WORD_ID;
                    return Predefine.TAG_CLUSTER;
                }
//                case xx:
//                case w:
//                {
//                    word= Predefine.TAG_OTHER;
//                }
//                break;
                case t:
                {
                    wordID = CoreDictionary.T_WORD_ID;
                    return Predefine.TAG_TIME;
                }
            }
        }

        return realWord;
    }

    
    public Vertex(String realWord, CoreDictionary.Attribute attribute)
    {
        this(null, realWord, attribute);
    }

    public Vertex(String realWord, CoreDictionary.Attribute attribute, int wordID)
    {
        this(null, realWord, attribute, wordID);
    }

    
    public Vertex(Map.Entry<String, CoreDictionary.Attribute> entry)
    {
        this(entry.getKey(), entry.getValue());
    }

    
    public Vertex(String realWord)
    {
        this(null, realWord, CoreDictionary.get(realWord));
    }

    public Vertex(char realWord, CoreDictionary.Attribute attribute)
    {
        this(String.valueOf(realWord), attribute);
    }

    
    public String getRealWord()
    {
        return realWord;
    }

    
    public CoreDictionary.Attribute getAttribute()
    {
        return attribute;
    }

    
    public boolean confirmNature(Nature nature)
    {
        if (attribute.nature.length == 1 && attribute.nature[0] == nature)
        {
            return true;
        }
        boolean result = true;
        int frequency = attribute.getNatureFrequency(nature);
        if (frequency == 0)
        {
            frequency = 1000;
            result = false;
        }
        attribute = new CoreDictionary.Attribute(nature, frequency);
        return result;
    }

    
    public boolean confirmNature(Nature nature, boolean updateWord)
    {
        switch (nature)
        {

            case m:
                word = Predefine.TAG_NUMBER;
                break;
            case t:
                word = Predefine.TAG_TIME;
                break;
            default:
                logger.warning("没有与" + nature + "对应的case");
                break;
        }

        return confirmNature(nature);
    }

    
    public Nature getNature()
    {
        if (attribute.nature.length == 1)
        {
            return attribute.nature[0];
        }

        return null;
    }

    
    public Nature guessNature()
    {
        return attribute.nature[0];
    }

    public boolean hasNature(Nature nature)
    {
        return attribute.getNatureFrequency(nature) > 0;
    }

    
    public Vertex copy()
    {
        return new Vertex(word, realWord, attribute);
    }

    public Vertex setWord(String word)
    {
        this.word = word;
        return this;
    }

    public Vertex setRealWord(String realWord)
    {
        this.realWord = realWord;
        return this;
    }

    
    public static Vertex newNumberInstance(String realWord)
    {
        return new Vertex(Predefine.TAG_NUMBER, realWord, new CoreDictionary.Attribute(Nature.m, 1000));
    }

    
    public static Vertex newAddressInstance(String realWord)
    {
        return new Vertex(Predefine.TAG_PLACE, realWord, new CoreDictionary.Attribute(Nature.ns, 1000));
    }

    
    public static Vertex newPunctuationInstance(String realWord)
    {
        return new Vertex(realWord, new CoreDictionary.Attribute(Nature.w, 1000));
    }

    
    public static Vertex newPersonInstance(String realWord)
    {
        return newPersonInstance(realWord, 1000);
    }

    
    public static Vertex newTranslatedPersonInstance(String realWord, int frequency)
    {
        return new Vertex(Predefine.TAG_PEOPLE, realWord, new CoreDictionary.Attribute(Nature.nrf, frequency));
    }

    
    public static Vertex newJapanesePersonInstance(String realWord, int frequency)
    {
        return new Vertex(Predefine.TAG_PEOPLE, realWord, new CoreDictionary.Attribute(Nature.nrj, frequency));
    }

    
    public static Vertex newPersonInstance(String realWord, int frequency)
    {
        return new Vertex(Predefine.TAG_PEOPLE, realWord, new CoreDictionary.Attribute(Nature.nr, frequency));
    }

    
    public static Vertex newPlaceInstance(String realWord, int frequency)
    {
        return new Vertex(Predefine.TAG_PLACE, realWord, new CoreDictionary.Attribute(Nature.ns, frequency));
    }

    
    public static Vertex newOrganizationInstance(String realWord, int frequency)
    {
        return new Vertex(Predefine.TAG_GROUP, realWord, new CoreDictionary.Attribute(Nature.nt, frequency));
    }

    
    public static Vertex newTimeInstance(String realWord)
    {
        return new Vertex(Predefine.TAG_TIME, realWord, new CoreDictionary.Attribute(Nature.t, 1000));
    }

    
    public static Vertex newB()
    {
        return new Vertex(Predefine.TAG_BIGIN, " ", new CoreDictionary.Attribute(Nature.begin, Predefine.MAX_FREQUENCY / 10), CoreDictionary.getWordID(Predefine.TAG_BIGIN));
    }

    
    public static Vertex newE()
    {
        return new Vertex(Predefine.TAG_END, " ", new CoreDictionary.Attribute(Nature.end, Predefine.MAX_FREQUENCY / 10), CoreDictionary.getWordID(Predefine.TAG_END));
    }

    @Override
    public String toString()
    {
        return realWord;
//        return "WordNode{" +
//                "word='" + word + '\'' +
//                (word.equals(realWord) ? "" : (", realWord='" + realWord + '\'')) +
////                ", attribute=" + attribute +
//                '}';
    }
}
