
package in.thyferny.nlp.corpus.dictionary.item;

import java.util.*;


public class Item extends SimpleItem
{
    
    public String key;

    public Item(String key, String label)
    {
        this(key);
        labelMap.put(label, 1);
    }

    public Item(String key)
    {
        super();
        this.key = key;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(key);
        ArrayList<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(labelMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>()
        {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
            {
                return -o1.getValue().compareTo(o2.getValue());
            }
        });
        for (Map.Entry<String, Integer> entry : entries)
        {
            sb.append(' ');             // 现阶段词典分隔符统一使用空格
            sb.append(entry.getKey());
            sb.append(' ');
            sb.append(entry.getValue());
        }
        return sb.toString();
    }

    
    public static Item create(String param)
    {
        if (param == null) return null;
        String mark = "\\s";    // 分隔符，历史格式用空格，但是现在觉得用制表符比较好
        if (param.indexOf('\t') > 0) mark = "\t";
        String[] array = param.split(mark);
        return create(array);
    }

    public static Item create(String param[])
    {
        if (param.length % 2 == 0) return null;
        Item item = new Item(param[0]);
        int natureCount = (param.length - 1) / 2;
        for (int i = 0; i < natureCount; ++i)
        {
            item.labelMap.put(param[1 + 2 * i], Integer.parseInt(param[2 + 2 * i]));
        }
        return item;
    }
}
