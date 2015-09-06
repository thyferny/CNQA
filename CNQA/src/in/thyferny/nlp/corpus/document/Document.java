
package in.thyferny.nlp.corpus.document;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.thyferny.nlp.corpus.document.sentence.Sentence;
import in.thyferny.nlp.corpus.document.sentence.word.CompoundWord;
import in.thyferny.nlp.corpus.document.sentence.word.IWord;
import in.thyferny.nlp.corpus.document.sentence.word.Word;

public class Document implements Serializable
{
    public List<Sentence> sentenceList;

    public Document(List<Sentence> sentenceList)
    {
        this.sentenceList = sentenceList;
    }

    public static Document create(String param)
    {
        Pattern pattern = Pattern.compile(".+?((。/w)|(！/w )|(？/w )|\\n|$)");
        Matcher matcher = pattern.matcher(param);
        List<Sentence> sentenceList = new LinkedList<Sentence>();
        while (matcher.find())
        {
            String single = matcher.group();
            Sentence sentence = Sentence.create(single);
            if (sentence == null)
            {
                logger.warning("使用" + single + "构建句子失败");
                return null;
            }
            sentenceList.add(sentence);
        }
        return new Document(sentenceList);
    }

    
    public List<IWord> getWordList()
    {
        List<IWord> wordList = new LinkedList<IWord>();
        for (Sentence sentence : sentenceList)
        {
            wordList.addAll(sentence.wordList);
        }
        return wordList;
    }

    public List<Word> getSimpleWordList()
    {
        List<IWord> wordList = getWordList();
        List<Word> simpleWordList = new LinkedList<Word>();
        for (IWord word : wordList)
        {
            if (word instanceof CompoundWord)
            {
                simpleWordList.addAll(((CompoundWord) word).innerList);
            }
            else
            {
                simpleWordList.add((Word) word);
            }
        }

        return simpleWordList;
    }

    
    public List<List<Word>> getSimpleSentenceList()
    {
        List<List<Word>> simpleList = new LinkedList<List<Word>>();
        for (Sentence sentence : sentenceList)
        {
            List<Word> wordList = new LinkedList<Word>();
            for (IWord word : sentence.wordList)
            {
                if (word instanceof CompoundWord)
                {
                    for (Word inner : ((CompoundWord) word).innerList)
                    {
                        wordList.add(inner);
                    }
                }
                else
                {
                    wordList.add((Word) word);
                }
            }
            simpleList.add(wordList);
        }

        return simpleList;
    }

    
    public List<List<IWord>> getComplexSentenceList()
    {
        List<List<IWord>> complexList = new LinkedList<List<IWord>>();
        for (Sentence sentence : sentenceList)
        {
            complexList.add(sentence.wordList);
        }

        return complexList;
    }

    
    public List<List<Word>> getSimpleSentenceList(boolean spilt)
    {
        List<List<Word>> simpleList = new LinkedList<List<Word>>();
        for (Sentence sentence : sentenceList)
        {
            List<Word> wordList = new LinkedList<Word>();
            for (IWord word : sentence.wordList)
            {
                if (word instanceof CompoundWord)
                {
                    if (spilt)
                    {
                        for (Word inner : ((CompoundWord) word).innerList)
                        {
                            wordList.add(inner);
                        }
                    }
                    else
                    {
                        wordList.add(((CompoundWord) word).toWord());
                    }
                }
                else
                {
                    wordList.add((Word) word);
                }
            }
            simpleList.add(wordList);
        }

        return simpleList;
    }

    
    public List<List<Word>> getSimpleSentenceList(Set<String> labelSet)
    {
        List<List<Word>> simpleList = new LinkedList<List<Word>>();
        for (Sentence sentence : sentenceList)
        {
            List<Word> wordList = new LinkedList<Word>();
            for (IWord word : sentence.wordList)
            {
                if (word instanceof CompoundWord)
                {
                    if (labelSet.contains(word.getLabel()))
                    {
                        for (Word inner : ((CompoundWord) word).innerList)
                        {
                            wordList.add(inner);
                        }
                    }
                    else
                    {
                        wordList.add(((CompoundWord) word).toWord());
                    }
                }
                else
                {
                    wordList.add((Word) word);
                }
            }
            simpleList.add(wordList);
        }

        return simpleList;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Sentence sentence : sentenceList)
        {
            sb.append(sentence);
            sb.append(' ');
        }
        if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
