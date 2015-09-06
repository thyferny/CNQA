package in.thyferny.ml.classifier.bayes;  
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;  
  
public class BayesModel {  
      
    public ArrayList<String> readTestData() throws IOException{  
        ArrayList<String> candAttr = new ArrayList<String>();  
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));  
        String str = "";  
        while (!(str = reader.readLine()).equals("")) {  
            StringTokenizer tokenizer = new StringTokenizer(str);  
            while (tokenizer.hasMoreTokens()) {  
                candAttr.add(tokenizer.nextToken());  
            }  
        }  
        return candAttr;  
    }  
      
    public ArrayList<ArrayList<String>> readData() throws IOException {  
        ArrayList<ArrayList<String>> datas = new ArrayList<ArrayList<String>>();
        FileReader fr = new FileReader(new File("data/questions-train.txt"));
        BufferedReader reader = new BufferedReader(fr);  
        String str = "";  
        while ((str = reader.readLine())!=null) {  
        	String line = str.substring(2);
            ArrayList<String> s = new ArrayList<String>();
            for(String word:line.split(" ")){
            	s.add(word);
            }
            s.add(String.valueOf(str.charAt(0)));
            datas.add(s);  
        }
        reader.close();
        return datas;  
    }  
      
    public static void main(String[] args) {  
        BayesModel tb = new BayesModel();  
        ArrayList<ArrayList<String>> datas = null;  
        ArrayList<String> testT = null;  
        Bayes bayes = new Bayes();  
        try {  
            datas = tb.readData(); 
            while (true) {  
                System.out.println("请输入测试元组");  
                testT = tb.readTestData();  
                String c = bayes.predictClass(datas, testT);  
                System.out.println("The class is: " + c);  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
}  