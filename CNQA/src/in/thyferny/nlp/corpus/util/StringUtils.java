
package in.thyferny.nlp.corpus.util;

import java.util.regex.Pattern;


public class StringUtils
{

    
    public static final String PATTERN = "&|[\uFE30-\uFFA0]|‘’|“”";

    public static String replaceSpecialtyStr(String str, String pattern, String replace)
    {
        if (isBlankOrNull(pattern))
            pattern = "\\s*|\t|\r|\n";//去除字符串中空格、换行、制表
        if (isBlankOrNull(replace))
            replace = "";
        return Pattern.compile(pattern).matcher(str).replaceAll(replace);

    }

    public static boolean isBlankOrNull(String str)
    {
        if (null == str) return true;
        //return str.length()==0?true:false;
        return str.length() == 0;
    }

    
    public static String cleanBlankOrDigit(String str)
    {
        if (isBlankOrNull(str)) return "null";
        return Pattern.compile("\\d|\\s").matcher(str).replaceAll("");
    }


    
    public static void main(String[] args)
    {
        System.out.println(replaceSpecialtyStr("中国电信2011年第一批IT设备集中采购-存储备份&（），)(UNIX服务器", PATTERN, ""));
    }
}


