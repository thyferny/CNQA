

package in.thyferny.nlp.collection.MDAG;

import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;



public class MDAGNode
{
    //The boolean denoting the accept state status of this node
    
    private boolean isAcceptNode;
    
    //The TreeMap to contain entries that represent a _transition (label and target node)
    
    private final TreeMap<Character, MDAGNode> outgoingTransitionTreeMap;

    //The int representing this node's incoming _transition node count
    
    private int incomingTransitionCount = 0;
    
    //The int denoting index in a simplified mdag data array that this node's _transition set begins at
    
    private int transitionSetBeginIndex = -1;
    
    //The int which will store this node's hash code after its been calculated (necessary due to how expensive the hashing calculation is)
    
    private Integer storedHashCode = null;
    
    
    
    
    public MDAGNode(boolean isAcceptNode)
    {
        this.isAcceptNode = isAcceptNode;     
        outgoingTransitionTreeMap = new TreeMap<Character, MDAGNode>();
    }

    
    
    
    private MDAGNode(MDAGNode node)
    {
        isAcceptNode = node.isAcceptNode;
        outgoingTransitionTreeMap = new TreeMap<Character, MDAGNode>(node.outgoingTransitionTreeMap);
        
        //Loop through the nodes in this node's outgoing _transition set, incrementing the number of
        //incoming transitions of each by 1 (to account for this newly created node's outgoing transitions)
        for(Entry<Character, MDAGNode> transitionKeyValuePair : outgoingTransitionTreeMap.entrySet())
            transitionKeyValuePair.getValue().incomingTransitionCount++;
        /////
    }
    
    
    
    
    public MDAGNode clone()
    {
        return new MDAGNode(this);
    }
    
    
    
    
    public MDAGNode clone(MDAGNode soleParentNode, char parentToCloneTransitionLabelChar)
    {
        MDAGNode cloneNode = new MDAGNode(this);
        soleParentNode.reassignOutgoingTransition(parentToCloneTransitionLabelChar, this, cloneNode);
        
        return cloneNode;
    }
    
    

    
    public int getTransitionSetBeginIndex()
    {
        return transitionSetBeginIndex;
    }
    
    
    
    
    public int getOutgoingTransitionCount()
    {
        return outgoingTransitionTreeMap.size();
    }
    
    
    
    
    public int getIncomingTransitionCount()
    {
        return incomingTransitionCount;
    }
    
    
    
    
    public boolean isConfluenceNode()
    {
        return (incomingTransitionCount > 1);
    }
    
    
    
    
    public boolean isAcceptNode()
    {
        return isAcceptNode;
    }
    
    
    
    
    public void setAcceptStateStatus(boolean isAcceptNode)
    {
        this.isAcceptNode = isAcceptNode;
    }
    
    
    
    
    public void setTransitionSetBeginIndex(int transitionSetBeginIndex)
    {
        this.transitionSetBeginIndex = transitionSetBeginIndex;
    }
    
    
    
    
    public boolean hasOutgoingTransition(char letter)
    {
        return outgoingTransitionTreeMap.containsKey(letter);
    }
    
    
    
    
    public boolean hasTransitions()
    {
        return !outgoingTransitionTreeMap.isEmpty();
    }
    
    
    
    
    public MDAGNode transition(char letter)
    {
        return outgoingTransitionTreeMap.get(letter);
    }
    
    
    
    
    public MDAGNode transition(String str)
    {
        int charCount = str.length();
        MDAGNode currentNode = this;
        
        //Iteratively _transition through the MDAG using the chars in str
        for(int i = 0; i < charCount; i++)
        {
            currentNode = currentNode.transition(str.charAt(i));
            if(currentNode == null) break;
        }
        /////
        
        return currentNode;
    }

    public MDAGNode transition(char[] str)
    {
        int charCount = str.length;
        MDAGNode currentNode = this;

        //Iteratively _transition through the MDAG using the chars in str
        for(int i = 0; i < charCount; ++i)
        {
            currentNode = currentNode.transition(str[i]);
            if(currentNode == null) break;
        }
        /////

        return currentNode;
    }

    public MDAGNode transition(char[] str, int offset)
    {
        int charCount = str.length - offset;
        MDAGNode currentNode = this;

        //Iteratively _transition through the MDAG using the chars in str
        for(int i = 0; i < charCount; ++i)
        {
            currentNode = currentNode.transition(str[i + offset]);
            if(currentNode == null) break;
        }
        /////

        return currentNode;
    }

    
    public Stack<MDAGNode> getTransitionPathNodes(String str)
    {
        Stack<MDAGNode> nodeStack = new Stack<MDAGNode>();
        
        MDAGNode currentNode = this;
        int numberOfChars = str.length();
        
        //Iteratively _transition through the MDAG using the chars in str,
        //putting each encountered node in nodeStack
        for(int i = 0; i < numberOfChars && currentNode != null; i++)
        {
            currentNode = currentNode.transition(str.charAt(i));
            nodeStack.add(currentNode);
        }
        /////
         
        return nodeStack;
    }

    
    
    
    public TreeMap<Character, MDAGNode> getOutgoingTransitions()
    {
        return outgoingTransitionTreeMap;
    }
    
    
    
    
    public void decrementTargetIncomingTransitionCounts()
    {
        for(Entry<Character, MDAGNode> transitionKeyValuePair: outgoingTransitionTreeMap.entrySet())
            transitionKeyValuePair.getValue().incomingTransitionCount--;
    }
    
    
    
    
    public void reassignOutgoingTransition(char letter, MDAGNode oldTargetNode, MDAGNode newTargetNode)
    {
        oldTargetNode.incomingTransitionCount--;
        newTargetNode.incomingTransitionCount++;
        
        outgoingTransitionTreeMap.put(letter, newTargetNode);
    }
    
    
    
    
    public MDAGNode addOutgoingTransition(char letter, boolean targetAcceptStateStatus)
    {
        MDAGNode newTargetNode = new MDAGNode(targetAcceptStateStatus);
        newTargetNode.incomingTransitionCount++;
        
        outgoingTransitionTreeMap.put(letter, newTargetNode);
        return newTargetNode;
    }

    
    public MDAGNode addOutgoingTransition(char letter, MDAGNode newTargetNode)
    {
        newTargetNode.incomingTransitionCount++;

        outgoingTransitionTreeMap.put(letter, newTargetNode);
        return newTargetNode;
    }
    
    
    
    
    public void removeOutgoingTransition(char letter)
    {
        outgoingTransitionTreeMap.remove(letter);
    }


    
    
    public static boolean haveSameTransitions(MDAGNode node1, MDAGNode node2)
    {
        TreeMap<Character, MDAGNode> outgoingTransitionTreeMap1 = node1.outgoingTransitionTreeMap;
        TreeMap<Character, MDAGNode> outgoingTransitionTreeMap2 = node2.outgoingTransitionTreeMap;
        
        if(outgoingTransitionTreeMap1.size() == outgoingTransitionTreeMap2.size())
        {
            //For each _transition in outgoingTransitionTreeMap1, get the identically lableed _transition
            //in outgoingTransitionTreeMap2 (if present), and test the equality of the transitions' target nodes
            for(Entry<Character, MDAGNode> transitionKeyValuePair : outgoingTransitionTreeMap1.entrySet())
            {
                Character currentCharKey = transitionKeyValuePair.getKey();
                MDAGNode currentTargetNode = transitionKeyValuePair.getValue();
                
                if(!outgoingTransitionTreeMap2.containsKey(currentCharKey) || !outgoingTransitionTreeMap2.get(currentCharKey).equals(currentTargetNode))
                    return false;
            }
            /////
        }
        else
            return false;
        
        return true;
    }
    
    
    
    
    public void clearStoredHashCode()
    {
        storedHashCode = null;
    }
    
    
    
    
    @Override
    public boolean equals(Object obj)
    {
        boolean areEqual = (this == obj);
        
        if(!areEqual && obj != null && obj.getClass().equals(MDAGNode.class))
        {
            MDAGNode node = (MDAGNode)obj;
            areEqual = (isAcceptNode == node.isAcceptNode && haveSameTransitions(this, node));
        }
       
        return areEqual;
    }

    
    
    
    @Override
    public int hashCode() {
        
        if(storedHashCode == null)
        {
            int hash = 7;
            hash = 53 * hash + (this.isAcceptNode ? 1 : 0);
            hash = 53 * hash + (this.outgoingTransitionTreeMap != null ? this.outgoingTransitionTreeMap.hashCode() : 0);    //recursively hashes the nodes in all the 
                                                                                                                                //_transition paths stemming from this node
            storedHashCode = hash;
            return hash;
        }
        else
            return storedHashCode;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("MDAGNode{");
        sb.append("isAcceptNode=").append(isAcceptNode);
        sb.append(", outgoingTransitionTreeMap=").append(outgoingTransitionTreeMap.keySet());
        sb.append(", incomingTransitionCount=").append(incomingTransitionCount);
//        sb.append(", transitionSetBeginIndex=").append(transitionSetBeginIndex);
//        sb.append(", storedHashCode=").append(storedHashCode);
        sb.append('}');
        return sb.toString();
    }
}
