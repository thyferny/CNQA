
package in.thyferny.nlp.corpus.document.sentence.word;

import static in.thyferny.nlp.utility.Predefine.logger;

public class Word implements IWord
{
    
    public String value;
    
    public String label;

    @Override
    public String toString()
    {
        return value + '/' + label;
    }

    public Word(String value, String label)
    {
        this.value = value;
        this.label = label;
    }

    
    public static Word create(String param)
    {
        if (param == null) return null;
        int cutIndex = param.lastIndexOf('/');
        if (cutIndex <= 0 || cutIndex == param.length() - 1)
        {
            logger.warning("使用 " + param + "创建单个单词失败");
            return null;
        }

        return new Word(param.substring(0, cutIndex), param.substring(cutIndex + 1));
    }

    @Override
    public String getValue()
    {
        return value;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public void setLabel(String label)
    {
        this.label = label;
    }

    @Override
    public void setValue(String value)
    {
        this.value = value;
    }
}
