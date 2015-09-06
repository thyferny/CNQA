
package in.thyferny.nlp.corpus.io;

import java.io.DataOutputStream;


public interface ICacheAble
{
    
    public void save(DataOutputStream out) throws Exception;

    public boolean load(ByteArray byteArray);
}
