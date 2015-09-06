
package in.thyferny.nlp.corpus.dependency.CoNll;

import in.thyferny.nlp.utility.Predefine;


public class PosTagCompiler
{
    
    public static String compile(String tag, String name)
    {
        if (tag.startsWith("m")) return Predefine.TAG_NUMBER;
        else if (tag.startsWith("nr")) return Predefine.TAG_PEOPLE;
        else if (tag.startsWith("ns")) return Predefine.TAG_PLACE;
        else if (tag.startsWith("nt")) return Predefine.TAG_GROUP;
        else if (tag.startsWith("t")) return Predefine.TAG_TIME;
        else if (tag.equals("x")) return Predefine.TAG_CLUSTER;
        else if (tag.equals("nx")) return Predefine.TAG_PROPER;
        else if (tag.equals("xx")) return Predefine.TAG_OTHER;

//        switch (tag)
//        {
//            case "m":
//            case "mq":
//                return Predefine.TAG_NUMBER;
//            case "nr":
//            case "nr1":
//            case "nr2":
//            case "nrf":
//            case "nrj":
//                return Predefine.TAG_PEOPLE;
//            case "ns":
//            case "nsf":
//                return Predefine.TAG_PLACE;
//            case "nt":
//                return Predefine.TAG_TIME;
//            case "x":
//                return Predefine.TAG_CLUSTER;
//            case "nx":
//                return Predefine.TAG_PROPER;
//        }

        return name;
    }
}
