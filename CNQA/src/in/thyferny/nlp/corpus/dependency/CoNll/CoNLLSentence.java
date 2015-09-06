
package in.thyferny.nlp.corpus.dependency.CoNll;

import java.util.List;


public class CoNLLSentence
{
    
    public CoNLLWord[] word;

    
    public CoNLLSentence(List<CoNllLine> lineList)
    {
        CoNllLine[] lineArray = lineList.toArray(new CoNllLine[0]);
        this.word = new CoNLLWord[lineList.size()];
        int i = 0;
        for (CoNllLine line : lineList)
        {
            word[i++] = new CoNLLWord(line);
        }
        for (CoNLLWord nllWord : word)
        {
            int head = Integer.parseInt(lineArray[nllWord.ID - 1].value[6]) - 1;
            if (head != -1)
            {
                nllWord.HEAD = word[head];
            }
            else
            {
                nllWord.HEAD = CoNLLWord.ROOT;
            }
        }
    }

    public CoNLLSentence(CoNLLWord[] word)
    {
        this.word = word;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(word.length * 50);
        for (CoNLLWord word : this.word)
        {
            sb.append(word);
            sb.append('\n');
        }
        return sb.toString();
    }

    
    public String[][] getEdgeArray()
    {
        String[][] edge = new String[word.length + 1][word.length + 1];
        for (CoNLLWord coNLLWord : word)
        {
            edge[coNLLWord.ID][coNLLWord.HEAD.ID] = coNLLWord.DEPREL;
        }

        return edge;
    }

    
    public CoNLLWord[] getWordArrayWithRoot()
    {
        CoNLLWord[] wordArray = new CoNLLWord[word.length + 1];
        wordArray[0] = CoNLLWord.ROOT;
        System.arraycopy(word, 0, wordArray, 1, word.length);

        return wordArray;
    }
}
