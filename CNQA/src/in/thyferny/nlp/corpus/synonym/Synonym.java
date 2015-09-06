
package in.thyferny.nlp.corpus.synonym;

import java.util.ArrayList;
import java.util.List;


public class Synonym implements ISynonym
{
    public String realWord;
    public long id;
    public Type type;

    @Deprecated
    public Synonym(String realWord, String idString)
    {
        this.realWord = realWord;
        id = SynonymHelper.convertString2Id(idString);
    }

    @Deprecated
    public Synonym(String realWord, long id)
    {
        this.realWord = realWord;
        this.id = id;
    }

    public Synonym(String realWord, long id, Type type)
    {
        this.realWord = realWord;
        this.id = id;
        this.type = type;
    }

    @Override
    public String getRealWord()
    {
        return realWord;
    }

    @Override
    public long getId()
    {
        return id;
    }

    @Override
    public String getIdString()
    {
        return SynonymHelper.convertId2StringWithIndex(id);
    }

    
    public static List<Synonym> create(String param)
    {
        if (param == null) return null;
        String[] args = param.split(" ");
        return create(args);
    }

    
    public static ArrayList<Synonym> create(String[] args)
    {
        ArrayList<Synonym> synonymList = new ArrayList<Synonym>(args.length - 1);

        String idString = args[0];
        Type type;
        switch (idString.charAt(idString.length() - 1))
        {
            case '=':
                type = Type.EQUAL;
                break;
            case '#':
                type = Type.LIKE;
                break;
            default:
                type = Type.SINGLE;
                break;
        }
        long startId = SynonymHelper.convertString2IdWithIndex(idString, 0);    // id从这里开始
        for (int i = 1; i < args.length; ++i)
        {
            if (type == Type.LIKE)
            {
                synonymList.add(new Synonym(args[i], startId + i, type));             // 如果不同则id递增
            }
            else
            {
                synonymList.add(new Synonym(args[i], startId, type));             // 如果相同则不变
            }
        }
        return synonymList;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(realWord);
        switch (type)
        {

            case EQUAL:
                sb.append('=');
                break;
            case LIKE:
                sb.append('#');
                break;
            case SINGLE:
                sb.append('@');
                break;
            case UNDEFINED:
                sb.append('?');
                break;
        }
        sb.append(getIdString());
        return sb.toString();
    }

    
    public long distance(Synonym other)
    {
        return Math.abs(id - other.id);
    }

    public static enum Type
    {
        
        EQUAL,
        
        LIKE,
        
        SINGLE,

        
        UNDEFINED,
    }
}
