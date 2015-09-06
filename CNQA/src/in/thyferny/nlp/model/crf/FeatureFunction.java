
package in.thyferny.nlp.model.crf;

import java.io.DataOutputStream;

import in.thyferny.nlp.corpus.io.ByteArray;
import in.thyferny.nlp.corpus.io.ICacheAble;


public class FeatureFunction implements ICacheAble
{
    
    char[] o;
    
//    String s;

    
    double[] w;

    public FeatureFunction(char[] o, int tagSize)
    {
        this.o = o;
        w = new double[tagSize];
    }

    public FeatureFunction()
    {
    }

    @Override
    public void save(DataOutputStream out) throws Exception
    {
        out.writeInt(o.length);
        for (char c : o)
        {
            out.writeChar(c);
        }
        out.writeInt(w.length);
        for (double v : w)
        {
            out.writeDouble(v);
        }
    }

    @Override
    public boolean load(ByteArray byteArray)
    {
        int size = byteArray.nextInt();
        o = new char[size];
        for (int i = 0; i < size; ++i)
        {
            o[i] = byteArray.nextChar();
        }
        size = byteArray.nextInt();
        w = new double[size];
        for (int i = 0; i < size; ++i)
        {
            w[i] = byteArray.nextDouble();
        }
        return true;
    }
}
