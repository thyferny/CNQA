
package in.thyferny.nlp.corpus.util;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;


public class DictionaryUtil
{
    
    public static boolean sortDictionary(String path)
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            TreeMap<String, String> map = new TreeMap<String, String>();
            String line;

            while ((line = br.readLine()) != null)
            {
                String[] param = line.split("\\s");
                map.put(param[0], line);
            }
            br.close();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
            for (Map.Entry<String, String> entry : map.entrySet())
            {
                bw.write(entry.getValue());
                bw.newLine();
            }
            bw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
