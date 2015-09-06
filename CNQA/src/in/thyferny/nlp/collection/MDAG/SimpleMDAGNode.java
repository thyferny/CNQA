
package in.thyferny.nlp.collection.MDAG;


import java.io.DataOutputStream;

import in.thyferny.nlp.corpus.io.ByteArray;
import in.thyferny.nlp.corpus.io.ICacheAble;


public class SimpleMDAGNode implements ICacheAble
{
    //The character labeling an incoming _transition to this node
    private char letter;

    //The boolean denoting the accept state status of this node
    private boolean isAcceptNode;

    //The int denoting the size of this node's outgoing _transition set
    private int transitionSetSize;

    //The int denoting the index (in the array which contains this node) at which this node's _transition set begins
    private int transitionSetBeginIndex;


    
    public SimpleMDAGNode(char letter, boolean isAcceptNode, int transitionSetSize)
    {
        this.letter = letter;
        this.isAcceptNode = isAcceptNode;
        this.transitionSetSize = transitionSetSize;
        this.transitionSetBeginIndex = 0;           //will be changed for all objects of this type, necessary for dummy root node creation
    }

    public SimpleMDAGNode()
    {

    }


    
    public char getLetter()
    {
        return letter;
    }


    
    public boolean isAcceptNode()
    {
        return isAcceptNode;
    }


    
    public int getTransitionSetBeginIndex()
    {
        return transitionSetBeginIndex;
    }


    
    public int getOutgoingTransitionSetSize()
    {
        return transitionSetSize;
    }


    
    public void setTransitionSetBeginIndex(int transitionSetBeginIndex)
    {
        this.transitionSetBeginIndex = transitionSetBeginIndex;
    }


    
    public SimpleMDAGNode transition(SimpleMDAGNode[] mdagDataArray, char letter)
    {
        SimpleMDAGNode targetNode = null;
        int offset = binarySearch(mdagDataArray, letter);
        if (offset >= 0)
        {
            targetNode = mdagDataArray[offset];
        }
        /////

        return targetNode;
    }

    private SimpleMDAGNode transitionBruteForce(SimpleMDAGNode[] mdagDataArray, char letter)
    {
        int onePastTransitionSetEndIndex = transitionSetBeginIndex + transitionSetSize;
        SimpleMDAGNode targetNode = null;

        //Loop through the SimpleMDAGNodes in this node's _transition set, searching for
        //the one with a letter equal to that which labels the desired _transition
        for(int i = transitionSetBeginIndex; i < onePastTransitionSetEndIndex; i++)
        {
            if(mdagDataArray[i].getLetter() == letter)
            {
                targetNode = mdagDataArray[i];
                break;
            }
        }
        /////

        return targetNode;
    }

    
    private int binarySearch(SimpleMDAGNode[] mdagDataArray, char node)
    {
        if (transitionSetSize < 1)
        {
            return -1;
        }
        int high = transitionSetBeginIndex + transitionSetSize - 1;
        int low = transitionSetBeginIndex;
        while (low <= high)
        {
            int mid = ((low + high) >>> 1);
            int cmp = mdagDataArray[mid].getLetter() - node;

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }
        return -1;
    }


    
    public SimpleMDAGNode transition(SimpleMDAGNode[] mdagDataArray, String str)
    {
        SimpleMDAGNode currentNode = this;
        int numberOfChars = str.length();

        //Iteratively _transition through the MDAG using the chars in str
        for(int i = 0; i < numberOfChars; i++)
        {
            currentNode = currentNode.transition(mdagDataArray, str.charAt(i));
            if(currentNode == null) break;
        }
        /////

        return currentNode;
    }

    public SimpleMDAGNode transition(SimpleMDAGNode[] mdagDataArray, char[] str)
    {
        SimpleMDAGNode currentNode = this;
        int numberOfChars = str.length;

        //Iteratively _transition through the MDAG using the chars in str
        for (int i = 0; i < numberOfChars; i++)
        {
            currentNode = currentNode.transition(mdagDataArray, str[i]);
            if (currentNode == null) break;
        }
        /////

        return currentNode;
    }

    public SimpleMDAGNode transition(SimpleMDAGNode[] mdagDataArray, char[] str, int offset)
    {
        SimpleMDAGNode currentNode = this;
        int numberOfChars = str.length - offset;

        //Iteratively _transition through the MDAG using the chars in str
        for (int i = 0; i < numberOfChars; i++)
        {
            currentNode = currentNode.transition(mdagDataArray, str[offset + i]);
            if (currentNode == null) break;
        }
        /////

        return currentNode;
    }


    
    public static SimpleMDAGNode traverseMDAG(SimpleMDAGNode[] mdagDataArray, SimpleMDAGNode sourceNode, String str)
    {
//        char firstLetter = str.charAt(0);

        //Loop through the SimpleMDAGNodes in the processing MDAG's source node's _transition set,
        //searching for the the one with a letter (char) equal to the first char of str.
        //We can use that target node to _transition through the MDAG with the rest of the string
        return sourceNode.transition(mdagDataArray, str.toCharArray());
//        for(int i = 0; i < sourceNode.transitionSetSize; i++)
//        {
//            if(mdagDataArray[i].getLetter() == firstLetter)
//                return mdagDataArray[i]._transition(mdagDataArray, str.substring(1));
//        }
//        /////
//
//        return null;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("SimpleMDAGNode{");
        sb.append("letter=").append(letter);
        sb.append(", isAcceptNode=").append(isAcceptNode);
        sb.append(", transitionSetSize=").append(transitionSetSize);
        sb.append(", transitionSetBeginIndex=").append(transitionSetBeginIndex);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void save(DataOutputStream out) throws Exception
    {
        out.writeChar(letter);
        out.writeByte(isAcceptNode ? 1 : 0);
        out.writeInt(transitionSetBeginIndex);
        out.writeInt(transitionSetSize);
    }

    @Override
    public boolean load(ByteArray byteArray)
    {
        letter = byteArray.nextChar();
        isAcceptNode = byteArray.nextByte() == 1;
        transitionSetBeginIndex = byteArray.nextInt();
        transitionSetSize = byteArray.nextInt();
        return true;
    }
}