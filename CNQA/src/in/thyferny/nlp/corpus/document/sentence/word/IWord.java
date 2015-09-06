
package in.thyferny.nlp.corpus.document.sentence.word;

import java.io.Serializable;


public interface IWord extends Serializable
{
    String getValue();
    String getLabel();
    void setLabel(String label);
    void setValue(String value);
}
