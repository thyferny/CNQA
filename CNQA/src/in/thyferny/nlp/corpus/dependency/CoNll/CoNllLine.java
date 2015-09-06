
package in.thyferny.nlp.corpus.dependency.CoNll;


public class CoNllLine
{
    
    public String[] value = new String[10];

    
    public int id;

    public CoNllLine(String... args)
    {
        int length = Math.min(args.length, value.length);
        for (int i = 0; i < length; ++i)
        {
            value[i] = args[i];
        }
        id = Integer.parseInt(value[0]);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        for (String value : this.value)
        {
            sb.append(value);
            sb.append('\t');
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}
