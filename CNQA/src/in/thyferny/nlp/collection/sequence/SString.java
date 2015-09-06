
package in.thyferny.nlp.collection.sequence;

import java.util.Arrays;


public class SString implements Comparable<SString>, CharSequence
{
    public char[] value;
    
    public int b;
    
    public int e;

    
    public SString(char[] value, int b, int e)
    {
        this.value = value;
        this.b = b;
        this.e = e;
    }

    public SString(String s)
    {
        value = s.toCharArray();
        b = 0;
        e = s.length();
    }

    @Override
    public boolean equals(Object anObject)
    {
        if (this == anObject)
        {
            return true;
        }
        if (anObject instanceof SString)
        {
            SString anotherString = (SString) anObject;
            int n = value.length;
            if (n == anotherString.value.length)
            {
                char v1[] = value;
                char v2[] = anotherString.value;
                int i = 0;
                while (n-- != 0)
                {
                    if (v1[i] != v2[i])
                        return false;
                    i++;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int length()
    {
        return e - b;
    }

    @Override
    public char charAt(int index)
    {
        return value[b + index];
    }

    @Override
    public CharSequence subSequence(int start, int end)
    {
        return new SString(value, b + start, b + end);
    }

    @Override
    public String toString()
    {
        return new String(value, b, e - b);
    }

    @Override
    public int compareTo(SString anotherString)
    {
        int len1 = value.length;
        int len2 = anotherString.value.length;
        int lim = Math.min(len1, len2);
        char v1[] = value;
        char v2[] = anotherString.value;

        int k = 0;
        while (k < lim)
        {
            char c1 = v1[k];
            char c2 = v2[k];
            if (c1 != c2)
            {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }

    public char[] toCharArray()
    {
        return Arrays.copyOfRange(value, b, e);
    }

    public static SString valueOf(char word)
    {
        SString s = new SString(new char[]{word}, 0, 1);

        return s;
    }

    public SString add(SString other)
    {
        char[] value = new char[length() + other.length()];
        System.arraycopy(this.value, b, value, 0, length());
        System.arraycopy(other.value, other.b, value, length(), other.length());
        b = 0;
        e = length() + other.length();
        this.value = value;

        return this;
    }
}
