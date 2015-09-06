package in.thyferny.a;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.thyferny.nlp.MyNLP;
import in.thyferny.nlp.dictionary.CoreSynonymDictionary;
import in.thyferny.nlp.model.maxent.MaxEnt;
import in.thyferny.nlp.seg.Segment;
import in.thyferny.nlp.seg.common.Term;
import javafx.util.Pair;

public class Test {
	public static void main(String[] args) throws IOException {
//		MaxEntDependencyModelMaker.makeModel(corpusLoadPath, modelSavePath);
//		System.out.println(MaxEntDependencyParser.compute("哪一个是红色的"));
		
		String content = "平时无不适，昨天晚上聚餐，喝了些酒，有点高了；早上感觉有些头晕，肚子也难受，上了几次厕所，便溏；然后伴有恶心，面色苍白，呕吐，这是怎么了？";
		List<String> keywordList = MyNLP.extractKeyword(content, 3);
		System.out.println(keywordList);
		
		Segment segment = MyNLP.newSegment();
		segment.enableIndexMode(true);
		segment.enablePartOfSpeechTagging(false);
		segment.enableNameRecognize(true);
		segment.enablePlaceRecognize(true);
		segment.enableOrganizationRecognize(true);
		segment.enableTranslatedNameRecognize(false);
		segment.enableCustomDictionary(false);
		segment.enableJapaneseNameRecognize(false);
		segment.enableAllNamedEntityRecognize(true);
		List<Term> termList = segment.seg("欢迎使用ansj_seg,(ansj中文分词)在这里如果你遇到什么问题都可以联系我.我一定尽我所能.帮助大家.ansj_seg更快,更准,更自由!");
		System.out.println(termList);
		
//		trainSaveMaxEnt();
		
		loadPredictMaxEnt();
		
		System.out.println("距离1\t" + CoreSynonymDictionary.distance("香蕉", "苹果"));
		System.out.println("距离2\t" + CoreSynonymDictionary.distance("腹泻", "肚子疼"));
		System.out.println("距离3\t" + CoreSynonymDictionary.distance("腹泻", "头疼"));
		System.out.println("距离4\t" + CoreSynonymDictionary.distance("腹泻", "胃病"));
		System.out.println("距离5\t" + CoreSynonymDictionary.distance("腹泻", "呕吐"));
		System.out.println("距离6\t" + CoreSynonymDictionary.distance("晕车", "呕吐"));
		System.out.println("距离7\t" + CoreSynonymDictionary.distance("晕车", "晕船"));
		System.out.println("距离8\t" + CoreSynonymDictionary.distance("晕车", "吃药"));
		System.out.println("距离9\t" + CoreSynonymDictionary.distance("晕车", "山路"));
	}
	
    public static void trainSaveMaxEnt() throws IOException
    {
        String path = "data/questions-train.txt";
        MaxEnt maxEnt = new MaxEnt();
        maxEnt.loadData(path);
        maxEnt.train(5);
        maxEnt.save();
    }
    
    public static void loadPredictMaxEnt() throws IOException
    {
        MaxEnt maxEnt = MaxEnt.loadModel();
        List<String> fieldList = new ArrayList<String>();
        fieldList.add("When");
        fieldList.add("was");
        Pair<String, Double>[] result = maxEnt.predict(fieldList);  // 预测QuestionType概率各是多少
        System.out.println(Arrays.toString(result));
        System.out.println(maxEnt.eval(fieldList));
    }
}
