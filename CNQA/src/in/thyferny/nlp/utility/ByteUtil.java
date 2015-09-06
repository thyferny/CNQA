
package in.thyferny.nlp.utility;

import java.io.DataOutputStream;
import java.io.IOException;


public class ByteUtil
{

    
    public static char bytesToChar(byte[] b)
    {
        char c = (char) ((b[0] << 8) & 0xFF00L);
        c |= (char) (b[1] & 0xFFL);
        return c;
    }

    
    public static double bytesToDouble(byte[] b)
    {
        return Double.longBitsToDouble(bytesToLong(b));
    }

    
    public static double bytesHighFirstToDouble(byte[] bytes, int start)
    {
        long l = ((long) bytes[start] << 56) & 0xFF00000000000000L;
        // 如果不强制转换为long，那么默认会当作int，导致最高32位丢失
        l |= ((long) bytes[1 + start] << 48) & 0xFF000000000000L;
        l |= ((long) bytes[2 + start] << 40) & 0xFF0000000000L;
        l |= ((long) bytes[3 + start] << 32) & 0xFF00000000L;
        l |= ((long) bytes[4 + start] << 24) & 0xFF000000L;
        l |= ((long) bytes[5 + start] << 16) & 0xFF0000L;
        l |= ((long) bytes[6 + start] << 8) & 0xFF00L;
        l |= (long) bytes[7 + start] & 0xFFL;

        return Double.longBitsToDouble(l);
    }

    
    public static float bytesToFloat(byte[] b)
    {
        return Float.intBitsToFloat(bytesToInt(b));
    }

    
    public static int bytesToInt(byte[] b)
    {
        int i = (b[0] << 24) & 0xFF000000;
        i |= (b[1] << 16) & 0xFF0000;
        i |= (b[2] << 8) & 0xFF00;
        i |= b[3] & 0xFF;
        return i;
    }

    
    public static long bytesToLong(byte[] b)
    {
        long l = ((long) b[0] << 56) & 0xFF00000000000000L;
        // 如果不强制转换为long，那么默认会当作int，导致最高32位丢失
        l |= ((long) b[1] << 48) & 0xFF000000000000L;
        l |= ((long) b[2] << 40) & 0xFF0000000000L;
        l |= ((long) b[3] << 32) & 0xFF00000000L;
        l |= ((long) b[4] << 24) & 0xFF000000L;
        l |= ((long) b[5] << 16) & 0xFF0000L;
        l |= ((long) b[6] << 8) & 0xFF00L;
        l |= (long) b[7] & 0xFFL;
        return l;
    }

    public static long bytesHighFirstToLong(byte[] b)
    {
        long l = ((long) b[0] << 56) & 0xFF00000000000000L;
        // 如果不强制转换为long，那么默认会当作int，导致最高32位丢失
        l |= ((long) b[1] << 48) & 0xFF000000000000L;
        l |= ((long) b[2] << 40) & 0xFF0000000000L;
        l |= ((long) b[3] << 32) & 0xFF00000000L;
        l |= ((long) b[4] << 24) & 0xFF000000L;
        l |= ((long) b[5] << 16) & 0xFF0000L;
        l |= ((long) b[6] << 8) & 0xFF00L;
        l |= (long) b[7] & 0xFFL;
        return l;
    }

    
    public static byte[] charToBytes(char c)
    {
        byte[] b = new byte[8];
        b[0] = (byte) (c >>> 8);
        b[1] = (byte) c;
        return b;
    }

    
    public static byte[] doubleToBytes(double d)
    {
        return longToBytes(Double.doubleToLongBits(d));
    }

    
    public static byte[] floatToBytes(float f)
    {
        return intToBytes(Float.floatToIntBits(f));
    }

    
    public static byte[] intToBytes(int i)
    {
        byte[] b = new byte[4];
        b[0] = (byte) (i >>> 24);
        b[1] = (byte) (i >>> 16);
        b[2] = (byte) (i >>> 8);
        b[3] = (byte) i;
        return b;
    }

    
    public static byte[] longToBytes(long l)
    {
        byte[] b = new byte[8];
        b[0] = (byte) (l >>> 56);
        b[1] = (byte) (l >>> 48);
        b[2] = (byte) (l >>> 40);
        b[3] = (byte) (l >>> 32);
        b[4] = (byte) (l >>> 24);
        b[5] = (byte) (l >>> 16);
        b[6] = (byte) (l >>> 8);
        b[7] = (byte) (l);
        return b;
    }

    
    public static int bytesToInt(byte[] bytes, int start)
    {
        int num = bytes[start] & 0xFF;
        num |= ((bytes[start + 1] << 8) & 0xFF00);
        num |= ((bytes[start + 2] << 16) & 0xFF0000);
        num |= ((bytes[start + 3] << 24) & 0xFF000000);
        return num;
    }

    
    public static int bytesHighFirstToInt(byte[] bytes, int start)
    {
        int num = bytes[start + 3] & 0xFF;
        num |= ((bytes[start + 2] << 8) & 0xFF00);
        num |= ((bytes[start + 1] << 16) & 0xFF0000);
        num |= ((bytes[start] << 24) & 0xFF000000);
        return num;
    }

    
    public static char bytesHighFirstToChar(byte[] bytes, int start)
    {
        char c = (char) (((bytes[start] & 0xFF) << 8) | (bytes[start + 1] & 0xFF));
        return c;
    }

    
    public static float bytesHighFirstToFloat(byte[] bytes, int start)
    {
        int l = bytesHighFirstToInt(bytes, start);
        return Float.intBitsToFloat(l);
    }

    
    public static void writeUnsignedInt(DataOutputStream out, int uint) throws IOException
    {
        out.writeByte((byte) ((uint >>> 8) & 0xFF));
        out.writeByte((byte) ((uint >>> 0) & 0xFF));
    }

    public static int convertTwoCharToInt(char high, char low)
    {
        int result = high << 16;
        result |= low;
        return result;
    }

    public static char[] convertIntToTwoChar(int n)
    {
        char[] result = new char[2];
        result[0] = (char) (n >>> 16);
        result[1] = (char) (0x0000FFFF & n);
        return result;
    }
}
