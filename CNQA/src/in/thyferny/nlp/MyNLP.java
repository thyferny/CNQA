
package in.thyferny.nlp;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import in.thyferny.nlp.corpus.dependency.CoNll.CoNLLSentence;
import in.thyferny.nlp.dependency.MaxEntDependencyParser;
import in.thyferny.nlp.dictionary.py.Pinyin;
import in.thyferny.nlp.dictionary.py.PinyinDictionary;
import in.thyferny.nlp.dictionary.ts.SimplifiedChineseDictionary;
import in.thyferny.nlp.dictionary.ts.TraditionalChineseDictionary;
import in.thyferny.nlp.phrase.IPhraseExtractor;
import in.thyferny.nlp.phrase.MutualInformationEntropyPhraseExtractor;
import in.thyferny.nlp.seg.Segment;
import in.thyferny.nlp.seg.Viterbi.ViterbiSegment;
import in.thyferny.nlp.seg.common.Term;
import in.thyferny.nlp.summary.TextRankKeyword;
import in.thyferny.nlp.summary.TextRankSentence;
import in.thyferny.nlp.tokenizer.StandardTokenizer;


public class MyNLP
{
    
    public static final class Config
    {
        
        public static boolean DEBUG = false;
        
        public static String CoreDictionaryPath = "data/dictionary/CoreNatureDictionary.txt";
        
        public static String CoreDictionaryTransformMatrixDictionaryPath = "data/dictionary/CoreNatureDictionary.tr.txt";
        
        public static String CustomDictionaryPath[] = new String[]{"data/dictionary/custom/CustomDictionary.txt"};
        
        public static String BiGramDictionaryPath = "data/dictionary/CoreNatureDictionary.ngram.txt";

        
        public static String CoreStopWordDictionaryPath = "data/dictionary/stopwords.txt";
        
        public static String CoreSynonymDictionaryDictionaryPath = "data/dictionary/synonym/CoreSynonym.txt";
        
        public static String PersonDictionaryPath = "data/dictionary/person/nr.txt";
        
        public static String PersonDictionaryTrPath = "data/dictionary/person/nr.tr.txt";
        
        public static String PlaceDictionaryPath = "data/dictionary/place/ns.txt";
        
        public static String PlaceDictionaryTrPath = "data/dictionary/place/ns.tr.txt";
        
        public static String OrganizationDictionaryPath = "data/dictionary/organization/nt.txt";
        
        public static String OrganizationDictionaryTrPath = "data/dictionary/organization/nt.tr.txt";
        
        public static String TraditionalChineseDictionaryPath = "data/dictionary/tc/TraditionalChinese.txt";
        
        public static String SYTDictionaryPath = "data/dictionary/pinyin/SYTDictionary.txt";

        
        public static String PinyinDictionaryPath = "data/dictionary/pinyin/pinyin.txt";

        
        public static String TranslatedPersonDictionaryPath = "data/dictionary/person/nrf.txt";

        
        public static String JapanesePersonDictionaryPath = "data/dictionary/person/nrj.txt";

        
        public static String CharTypePath = "data/dictionary/other/CharType.dat.yes";

        
        public static String CharTablePath = "data/dictionary/other/CharTable.bin.yes";

        
        public static String WordNatureModelPath = "data/model/dependency/WordNature.txt";

        
        public static String MaxEntModelPath = "data/model/dependency/MaxEntModel.txt";
        
        public static String CRFSegmentModelPath = "data/model/segment/CRFSegmentModel.txt";
        
        public static String HMMSegmentModelPath = "data/model/segment/HMMSegmentModel.bin";
        
        public static String CRFDependencyModelPath = "data/model/dependency/CRFDependencyModelMini.txt";
        
        public static boolean ShowTermNature = true;
        
        public static boolean Normalization = false;

        static
        {
            // 自动读取配置
            Properties p = new Properties();
            try
            {
                p.load(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("hanlp.properties"), "UTF-8"));
                String root = p.getProperty("root", "").replaceAll("\\\\", "/");
                if (!root.endsWith("/")) root += "/";
                CoreDictionaryPath = root + p.getProperty("CoreDictionaryPath", CoreDictionaryPath);
                CoreDictionaryTransformMatrixDictionaryPath = root + p.getProperty("CoreDictionaryTransformMatrixDictionaryPath", CoreDictionaryTransformMatrixDictionaryPath);
                BiGramDictionaryPath = root + p.getProperty("BiGramDictionaryPath", BiGramDictionaryPath);
                CoreStopWordDictionaryPath = root + p.getProperty("CoreStopWordDictionaryPath", CoreStopWordDictionaryPath);
                CoreSynonymDictionaryDictionaryPath = root + p.getProperty("CoreSynonymDictionaryDictionaryPath", CoreSynonymDictionaryDictionaryPath);
                PersonDictionaryPath = root + p.getProperty("PersonDictionaryPath", PersonDictionaryPath);
                PersonDictionaryTrPath = root + p.getProperty("PersonDictionaryTrPath", PersonDictionaryTrPath);
                String[] pathArray = p.getProperty("CustomDictionaryPath", "dictionary/custom/CustomDictionary.txt").split(";");
                String prePath = root;
                for (int i = 0; i < pathArray.length; ++i)
                {
                    if (pathArray[i].startsWith(" "))
                    {
                        pathArray[i] = prePath + pathArray[i].trim();
                    }
                    else
                    {
                        pathArray[i] = root + pathArray[i];
                        int lastSplash = pathArray[i].lastIndexOf('/');
                        if (lastSplash != -1)
                        {
                            prePath = pathArray[i].substring(0, lastSplash + 1);
                        }
                    }
                }
                CustomDictionaryPath = pathArray;
                TraditionalChineseDictionaryPath = root + p.getProperty("TraditionalChineseDictionaryPath", TraditionalChineseDictionaryPath);
                SYTDictionaryPath = root + p.getProperty("SYTDictionaryPath", SYTDictionaryPath);
                PinyinDictionaryPath = root + p.getProperty("PinyinDictionaryPath", PinyinDictionaryPath);
                TranslatedPersonDictionaryPath = root + p.getProperty("TranslatedPersonDictionaryPath", TranslatedPersonDictionaryPath);
                JapanesePersonDictionaryPath = root + p.getProperty("JapanesePersonDictionaryPath", JapanesePersonDictionaryPath);
                PlaceDictionaryPath = root + p.getProperty("PlaceDictionaryPath", PlaceDictionaryPath);
                PlaceDictionaryTrPath = root + p.getProperty("PlaceDictionaryTrPath", PlaceDictionaryTrPath);
                OrganizationDictionaryPath = root + p.getProperty("OrganizationDictionaryPath", OrganizationDictionaryPath);
                OrganizationDictionaryTrPath = root + p.getProperty("OrganizationDictionaryTrPath", OrganizationDictionaryTrPath);
                CharTypePath = root + p.getProperty("CharTypePath", CharTypePath);
                CharTablePath = root + p.getProperty("CharTablePath", CharTablePath);
                WordNatureModelPath = root + p.getProperty("WordNatureModelPath", WordNatureModelPath);
                MaxEntModelPath = root + p.getProperty("MaxEntModelPath", MaxEntModelPath);
                CRFSegmentModelPath = root + p.getProperty("CRFSegmentModelPath", CRFSegmentModelPath);
                CRFDependencyModelPath = root + p.getProperty("CRFDependencyModelPath", CRFDependencyModelPath);
                HMMSegmentModelPath = root + p.getProperty("HMMSegmentModelPath", HMMSegmentModelPath);
                ShowTermNature = "true".equals(p.getProperty("ShowTermNature", "true"));
                Normalization = "true".equals(p.getProperty("Normalization", "false"));
            }
            catch (Exception e)
            {
            }
        }

        
        public static void enableDebug()
        {
            enableDebug(true);
        }

        
        public static void enableDebug(boolean enable)
        {
            DEBUG = enable;
            if (DEBUG)
            {
                logger.setLevel(Level.ALL);
            }
            else
            {
                logger.setLevel(Level.OFF);
            }
        }
    }

    
    private MyNLP() {}

    
    public static String convertToSimplifiedChinese(String traditionalChineseString)
    {
        return TraditionalChineseDictionary.convertToSimplifiedChinese(traditionalChineseString.toCharArray());
    }

    
    public static String convertToTraditionalChinese(String simplifiedChineseString)
    {
        return SimplifiedChineseDictionary.convertToTraditionalChinese(simplifiedChineseString.toCharArray());
    }

    
    public static String convertToPinyinString(String text, String separator, boolean remainNone)
    {
        List<Pinyin> pinyinList = PinyinDictionary.convertToPinyin(text, remainNone);
        int length = pinyinList.size();
        StringBuilder sb = new StringBuilder(length * (5 + separator.length()));
        int i = 1;
        for (Pinyin pinyin : pinyinList)
        {
            sb.append(pinyin.getPinyinWithoutTone());
            if (i < length)
            {
                sb.append(separator);
            }
            ++i;
        }
        return sb.toString();
    }

    
    public static List<Pinyin> convertToPinyinList(String text)
    {
        return PinyinDictionary.convertToPinyin(text);
    }

    
    public static String convertToPinyinFirstCharString(String text, String separator, boolean remainNone)
    {
        List<Pinyin> pinyinList = PinyinDictionary.convertToPinyin(text, remainNone);
        int length = pinyinList.size();
        StringBuilder sb = new StringBuilder(length * (1 + separator.length()));
        int i = 1;
        for (Pinyin pinyin : pinyinList)
        {
            sb.append(pinyin.getFirstChar());
            if (i < length)
            {
                sb.append(separator);
            }
            ++i;
        }
        return sb.toString();
    }

    
    public static List<Term> segment(String text)
    {
        return StandardTokenizer.segment(text.toCharArray());
    }

    
    public static Segment newSegment()
    {
        return new ViterbiSegment();   // Viterbi分词器是目前效率和效果的最佳平衡
    }

    
    public static CoNLLSentence parseDependency(String sentence)
    {
        return MaxEntDependencyParser.compute(sentence);
    }

    
    public static List<String> extractPhrase(String text, int size)
    {
        IPhraseExtractor extractor = new MutualInformationEntropyPhraseExtractor();
        return extractor.extractPhrase(text, size);
    }

    
    public static List<String> extractKeyword(String document, int size)
    {
        return TextRankKeyword.getKeywordList(document, size);
    }

    
    public static List<String> extractSummary(String document, int size)
    {
        return TextRankSentence.getTopSentenceList(document, size);
    }
}
