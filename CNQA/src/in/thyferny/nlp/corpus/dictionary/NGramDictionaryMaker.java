
package in.thyferny.nlp.corpus.dictionary;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import in.thyferny.nlp.collection.trie.bintrie.BinTrie;
import in.thyferny.nlp.corpus.document.sentence.word.IWord;


public class NGramDictionaryMaker
{
    BinTrie<Integer> trie;
    
    TMDictionaryMaker tmDictionaryMaker;

    public NGramDictionaryMaker()
    {
        trie = new BinTrie<Integer>();
        tmDictionaryMaker = new TMDictionaryMaker();
    }

    public void addPair(IWord first, IWord second)
    {
        String combine = first.getValue() + "@" + second.getValue();
        Integer frequency = trie.get(combine);
        if (frequency == null) frequency = 0;
        trie.put(combine, frequency + 1);
        // 同时还要统计标签的转移情况
        tmDictionaryMaker.addPair(first.getLabel(), second.getLabel());
    }

    
    public boolean saveTxtTo(String path)
    {
        saveNGramToTxt(path + ".ngram.txt");
        saveTransformMatrixToTxt(path + ".tr.txt");
        return true;
    }

    
    public boolean saveNGramToTxt(String path)
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
            for (Map.Entry<String, Integer> entry : trie.entrySet())
            {
                bw.write(entry.getKey() + " " + entry.getValue());
                bw.newLine();
            }
            bw.close();
        }
        catch (Exception e)
        {
            logger.warning("在保存NGram词典到" + path + "时发生异常" + e);
            return false;
        }

        return true;
    }

    
    public boolean saveTransformMatrixToTxt(String path)
    {
        return tmDictionaryMaker.saveTxtTo(path);
    }
}
