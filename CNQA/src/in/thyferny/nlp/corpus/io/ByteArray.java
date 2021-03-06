
package in.thyferny.nlp.corpus.io;

import static in.thyferny.nlp.utility.Predefine.logger;

import in.thyferny.nlp.utility.ByteUtil;


public class ByteArray
{
    byte[] bytes;
    int offset;

    public ByteArray(byte[] bytes)
    {
        this.bytes = bytes;
    }

    
    public static ByteArray createByteArray(String path)
    {
        byte[] bytes = IOUtil.readBytes(path);
        if (bytes == null) return null;
        return new ByteArray(bytes);
    }

    
    public byte[] getBytes()
    {
        return bytes;
    }

    
    public int nextInt()
    {
        int result = ByteUtil.bytesHighFirstToInt(bytes, offset);
        offset += 4;
        return result;
    }

    public double nextDouble()
    {
        double result = ByteUtil.bytesHighFirstToDouble(bytes, offset);
        offset += 8;
        return result;
    }

    
    public char nextChar()
    {
        char result = ByteUtil.bytesHighFirstToChar(bytes, offset);
        offset += 2;
        return result;
    }

    
    public byte nextByte()
    {
        return bytes[offset++];
    }

    public boolean hasMore()
    {
        return offset < bytes.length;
    }

    
    public String nextString()
    {
        StringBuilder sb = new StringBuilder();
        int length = nextInt();
        for (int i = 0; i < length; ++i)
        {
            sb.append(nextChar());
        }
        return sb.toString();
    }

    public float nextFloat()
    {
        float result = ByteUtil.bytesHighFirstToFloat(bytes, offset);
        offset += 4;
        return result;
    }

    
    public int nextUnsignedShort()
    {
        byte a = nextByte();
        byte b = nextByte();
        return (((a & 0xff) << 8) | (b & 0xff));
    }

    
    public String nextUTF()
    {
        int utflen = nextUnsignedShort();
        byte[] bytearr = null;
        char[] chararr = null;
        bytearr = new byte[utflen];
        chararr = new char[utflen];

        int c, char2, char3;
        int count = 0;
        int chararr_count = 0;

        for (int i = 0; i < utflen; ++i)
        {
            bytearr[i] = nextByte();
        }

        while (count < utflen)
        {
            c = (int) bytearr[count] & 0xff;
            if (c > 127) break;
            count++;
            chararr[chararr_count++] = (char) c;
        }

        while (count < utflen)
        {
            c = (int) bytearr[count] & 0xff;
            switch (c >> 4)
            {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    
                    count++;
                    chararr[chararr_count++] = (char) c;
                    break;
                case 12:
                case 13:
                    
                    count += 2;
                    if (count > utflen)
                        logger.severe(
                                "malformed input: partial character at end");
                    char2 = (int) bytearr[count - 1];
                    if ((char2 & 0xC0) != 0x80)
                        logger.severe(
                                "malformed input around byte " + count);
                    chararr[chararr_count++] = (char) (((c & 0x1F) << 6) |
                            (char2 & 0x3F));
                    break;
                case 14:
                    
                    count += 3;
                    if (count > utflen)
                        logger.severe(
                                "malformed input: partial character at end");
                    char2 = (int) bytearr[count - 2];
                    char3 = (int) bytearr[count - 1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        logger.severe(
                                "malformed input around byte " + (count - 1));
                    chararr[chararr_count++] = (char) (((c & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) |
                            ((char3 & 0x3F) << 0));
                    break;
                default:
                    
                    logger.severe(
                            "malformed input around byte " + count);
            }
        }
        // The number of chars produced may be less than utflen
        return new String(chararr, 0, chararr_count);
    }

    public int getOffset()
    {
        return offset;
    }

    public int getLength()
    {
        return bytes.length;
    }
}
