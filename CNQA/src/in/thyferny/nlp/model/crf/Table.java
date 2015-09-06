
package in.thyferny.nlp.model.crf;


public class Table
{
    
    public String[][] v;
    static final String HEAD = "_B";

    @Override
    public String toString()
    {
        if (v == null) return "null";
        final StringBuilder sb = new StringBuilder(v.length * v[0].length * 2);
        for (String[] line : v)
        {
            for (String element : line)
            {
                sb.append(element).append('\t');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    
    public String get(int x, int y)
    {
        if (x < 0) return HEAD + x;
        if (x >= v.length) return HEAD + "+" + (x - v.length + 1);

        return v[x][y];
    }

    public void setLast(int x, String t)
    {
        v[x][v[x].length - 1] = t;
    }

    public int size()
    {
        return v.length;
    }
}
