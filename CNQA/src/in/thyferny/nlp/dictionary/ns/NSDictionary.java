
package in.thyferny.nlp.dictionary.ns;


import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import in.thyferny.nlp.corpus.dictionary.item.EnumItem;
import in.thyferny.nlp.corpus.io.IOUtil;
import in.thyferny.nlp.corpus.tag.NS;
import in.thyferny.nlp.dictionary.common.CommonDictionary;
import in.thyferny.nlp.utility.ByteUtil;


public class NSDictionary extends CommonDictionary<EnumItem<NS>>
{
    @Override
    protected EnumItem<NS>[] onLoadValue(String path)
    {
        EnumItem<NS>[] valueArray = loadDat(path + ".value.dat");
        if (valueArray != null)
        {
            return valueArray;
        }
        List<EnumItem<NS>> valueList = new LinkedList<EnumItem<NS>>();
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null)
            {
                Map.Entry<String, Map.Entry<String, Integer>[]> args = EnumItem.create(line);
                EnumItem<NS> NSEnumItem = new EnumItem<NS>();
                for (Map.Entry<String, Integer> e : args.getValue())
                {
                    NSEnumItem.labelMap.put(NS.valueOf(e.getKey()), e.getValue());
                }
                valueList.add(NSEnumItem);
            }
            br.close();
        }
        catch (Exception e)
        {
            logger.warning("读取" + path + "失败" + e);
        }
        valueArray = valueList.toArray(new EnumItem[0]);
        return valueArray;
    }

    @Override
    protected boolean onSaveValue(EnumItem<NS>[] valueArray, String path)
    {
        return saveDat(path + ".value.dat", valueArray);
    }

    private EnumItem<NS>[] loadDat(String path)
    {
        byte[] bytes = IOUtil.readBytes(path);
        if (bytes == null) return null;
        NS[] NSArray = NS.values();
        int index = 0;
        int size = ByteUtil.bytesHighFirstToInt(bytes, index);
        index += 4;
        EnumItem<NS>[] valueArray = new EnumItem[size];
        for (int i = 0; i < size; ++i)
        {
            int currentSize = ByteUtil.bytesHighFirstToInt(bytes, index);
            index += 4;
            EnumItem<NS> item = new EnumItem<NS>();
            for (int j = 0; j < currentSize; ++j)
            {
                NS NS = NSArray[ByteUtil.bytesHighFirstToInt(bytes, index)];
                index += 4;
                int frequency = ByteUtil.bytesHighFirstToInt(bytes, index);
                index += 4;
                item.labelMap.put(NS, frequency);
            }
            valueArray[i] = item;
        }
        return valueArray;
    }

    private boolean saveDat(String path, EnumItem<NS>[] valueArray)
    {
        try
        {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(path));
            out.writeInt(valueArray.length);
            for (EnumItem<NS> item : valueArray)
            {
                out.writeInt(item.labelMap.size());
                for (Map.Entry<NS, Integer> entry : item.labelMap.entrySet())
                {
                    out.writeInt(entry.getKey().ordinal());
                    out.writeInt(entry.getValue());
                }
            }
            out.close();
        }
        catch (Exception e)
        {
            logger.warning("保存失败" + e);
            return false;
        }
        return true;
    }
}
