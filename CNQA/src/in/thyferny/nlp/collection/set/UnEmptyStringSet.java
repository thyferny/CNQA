
package in.thyferny.nlp.collection.set;

import java.util.TreeSet;


public class UnEmptyStringSet extends TreeSet<String>
{
    @Override
    public boolean add(String s)
    {
        if (s.trim().length() == 0) return false;

        return super.add(s);
    }
}
