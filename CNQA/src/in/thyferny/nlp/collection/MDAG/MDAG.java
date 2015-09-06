

package in.thyferny.nlp.collection.MDAG;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import in.thyferny.nlp.corpus.io.ByteArray;
import in.thyferny.nlp.corpus.io.ICacheAble;
import in.thyferny.nlp.corpus.io.IOUtil;



public class MDAG implements ICacheAble
{
    //MDAGNode from which all others in the structure are reachable (all manipulation and non-simplified MDAG search operations begin from this).
    
    protected MDAGNode sourceNode = new MDAGNode(false);

    //SimpleMDAGNode from which all others in the structure are reachable (will be defined if this MDAG is simplified)
    
    protected SimpleMDAGNode simplifiedSourceNode;

    //HashMap which contains the MDAGNodes collectively representing the all unique equivalence classes in the MDAG. 
    //Uniqueness is defined by the types of transitions allowed from, and number and type of nodes reachable
    //from the node of interest. Since there are no duplicate nodes in an MDAG, # of equivalence classes == # of nodes.
    
    protected HashMap<MDAGNode, MDAGNode> equivalenceClassMDAGNodeHashMap = new HashMap<MDAGNode, MDAGNode>();

    //Array that will contain a space-saving version of the MDAG after a call to simplify().
    
    protected SimpleMDAGNode[] mdagDataArray;

    //HashSet which will contain the set of unique characters used as _transition labels in the MDAG
    
    protected TreeSet<Character> charTreeSet = new TreeSet<Character>();

    //An int denoting the total number of transitions between the nodes of the MDAG
    
    protected int transitionCount;

    @Override
    public void save(DataOutputStream out) throws Exception
    {
        simplify();
        out.writeInt(charTreeSet.size());
        for (Character character : charTreeSet)
        {
            out.writeChar(character);
        }
        simplifiedSourceNode.save(out);
        out.writeInt(mdagDataArray.length);
        for (SimpleMDAGNode simpleMDAGNode : mdagDataArray)
        {
            simpleMDAGNode.save(out);
        }
    }

    @Override
    public boolean load(ByteArray byteArray)
    {
        int length = byteArray.nextInt();
        for (int i = 0; i < length; ++i)
        {
            charTreeSet.add(byteArray.nextChar());
        }
        simplifiedSourceNode = new SimpleMDAGNode();
        simplifiedSourceNode.load(byteArray);
        length = byteArray.nextInt();
        mdagDataArray = new SimpleMDAGNode[length];
        for (int i = 0; i < length; ++i)
        {
            mdagDataArray[i] = new SimpleMDAGNode();
            mdagDataArray[i].load(byteArray);
        }
        sourceNode = null;
        return true;
    }

    //Enum containing fields collectively denoting the set of all conditions that can be applied to a search on the MDAG

    
    private static enum SearchCondition
    {
        NO_SEARCH_CONDITION, PREFIX_SEARCH_CONDITION, SUBSTRING_SEARCH_CONDITION, SUFFIX_SEARCH_CONDITION;

        
        public boolean satisfiesCondition(String str1, String str2)
        {
            boolean satisfiesSearchCondition;

            switch (this)
            {
                case PREFIX_SEARCH_CONDITION:
                    satisfiesSearchCondition = (str1.startsWith(str2));
                    break;
                case SUBSTRING_SEARCH_CONDITION:
                    satisfiesSearchCondition = (str1.contains(str2));
                    break;
                case SUFFIX_SEARCH_CONDITION:
                    satisfiesSearchCondition = (str1.endsWith(str2));
                    break;
                default:
                    satisfiesSearchCondition = true;
                    break;
            }

            return satisfiesSearchCondition;
        }
    }
    /////


    
    public MDAG(File dataFile) throws IOException
    {
        BufferedReader dataFileBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), "UTF-8"));
        String currentString = "";
        String previousString = "";

        //Read all the lines in dataFile and add the String contained in each to the MDAG.
        while ((currentString = dataFileBufferedReader.readLine()) != null)
        {
            int mpsIndex = calculateMinimizationProcessingStartIndex(previousString, currentString);

            //If the _transition path of the previousString needs to be examined for minimization or
            //equivalence class representation after a certain point, call replaceOrRegister to do so.
            if (mpsIndex != -1)
            {
                String transitionSubstring = previousString.substring(0, mpsIndex);             // 公共前缀
                String minimizationProcessingSubstring = previousString.substring(mpsIndex);    // 不同后缀
                replaceOrRegister(sourceNode.transition(transitionSubstring), minimizationProcessingSubstring);
            }
            /////

            addStringInternal(currentString);
            previousString = currentString;
        }
        /////

        //Since we delay the minimization of the previously-added String
        //until after we read the next one, we need to have a seperate
        //statement to minimize the absolute last String.
        replaceOrRegister(sourceNode, previousString);
    }


    
    public MDAG(Collection<String> strCollection)
    {
        addStrings(strCollection);
    }

    
    public MDAG()
    {
    }

    
    public final void addStrings(Collection<String> strCollection)
    {
        if (sourceNode != null)
        {
            String previousString = "";

            //Add all the Strings in strCollection to the MDAG.
            for (String currentString : strCollection)
            {
                int mpsIndex = calculateMinimizationProcessingStartIndex(previousString, currentString);

                //If the _transition path of the previousString needs to be examined for minimization or
                //equivalence class representation after a certain point, call replaceOrRegister to do so.
                if (mpsIndex != -1)
                {

                    String transitionSubstring = previousString.substring(0, mpsIndex);
                    String minimizationProcessingSubString = previousString.substring(mpsIndex);
                    replaceOrRegister(sourceNode.transition(transitionSubstring), minimizationProcessingSubString);
                }
                /////

                addStringInternal(currentString);
                previousString = currentString;
            }
            /////

            //Since we delay the minimization of the previously-added String
            //until after we read the next one, we need to have a seperate
            //statement to minimize the absolute last String.
            replaceOrRegister(sourceNode, previousString);
        }
        else
        {
            unSimplify();
            addStrings(strCollection);
        }
    }


    
    public void addString(String str)
    {
        if (sourceNode != null)
        {
            addStringInternal(str);
            replaceOrRegister(sourceNode, str);
        }
        else
        {
            unSimplify();
            addString(str);
        }
    }


    private void splitTransitionPath(MDAGNode originNode, String storedStringSubstr)
    {
        HashMap<String, Object> firstConfluenceNodeDataHashMap = getTransitionPathFirstConfluenceNodeData(originNode, storedStringSubstr);
        Integer toFirstConfluenceNodeTransitionCharIndex = (Integer) firstConfluenceNodeDataHashMap.get("toConfluenceNodeTransitionCharIndex");
        MDAGNode firstConfluenceNode = (MDAGNode) firstConfluenceNodeDataHashMap.get("confluenceNode");

        if (firstConfluenceNode != null)
        {
            MDAGNode firstConfluenceNodeParent = originNode.transition(storedStringSubstr.substring(0, toFirstConfluenceNodeTransitionCharIndex));

            MDAGNode firstConfluenceNodeClone = firstConfluenceNode.clone(firstConfluenceNodeParent, storedStringSubstr.charAt(toFirstConfluenceNodeTransitionCharIndex));

            transitionCount += firstConfluenceNodeClone.getOutgoingTransitionCount();

            String unprocessedSubString = storedStringSubstr.substring(toFirstConfluenceNodeTransitionCharIndex + 1);
            splitTransitionPath(firstConfluenceNodeClone, unprocessedSubString);
        }
    }


    
    private int calculateSoleTransitionPathLength(String str)
    {
        Stack<MDAGNode> transitionPathNodeStack = sourceNode.getTransitionPathNodes(str);
        transitionPathNodeStack.pop();  //The MDAGNode at the top of the stack is not needed
        //(we are processing the outgoing transitions of nodes inside str's _transition path,
        //the outgoing transitions of the MDAGNode at the top of the stack are outside this path)

        transitionPathNodeStack.trimToSize();

        //Process each node in transitionPathNodeStack, using each to determine whether the
        //_transition path corresponding to str is only used by str.  This is true if and only if
        //each node in the _transition path has a single outgoing _transition and is not an accept state.
        while (!transitionPathNodeStack.isEmpty())
        {
            MDAGNode currentNode = transitionPathNodeStack.peek();
            if (currentNode.getOutgoingTransitions().size() <= 1 && !currentNode.isAcceptNode())
                transitionPathNodeStack.pop();
            else
                break;
        }
        /////

        return (transitionPathNodeStack.capacity() - transitionPathNodeStack.size());
    }


    
    public void removeString(String str)
    {
        if (sourceNode != null)
        {
            //Split the _transition path corresponding to str to ensure that
            //any other _transition paths sharing nodes with it are not affected
            splitTransitionPath(sourceNode, str);

            //Remove from equivalenceClassMDAGNodeHashMap, the entries of all the nodes in the _transition path corresponding to str.
            removeTransitionPathRegisterEntries(str);

            //Get the last node in the _transition path corresponding to str
            MDAGNode strEndNode = sourceNode.transition(str);
            if (strEndNode == null) return;

            if (!strEndNode.hasTransitions())
            {
                int soleInternalTransitionPathLength = calculateSoleTransitionPathLength(str);
                int internalTransitionPathLength = str.length() - 1;

                if (soleInternalTransitionPathLength == internalTransitionPathLength)
                {
                    sourceNode.removeOutgoingTransition(str.charAt(0));
                    transitionCount -= str.length();
                }
                else
                {
                    //Remove the sub-path in str's _transition path that is only used by str
                    int toBeRemovedTransitionLabelCharIndex = (internalTransitionPathLength - soleInternalTransitionPathLength);
                    MDAGNode latestNonSoloTransitionPathNode = sourceNode.transition(str.substring(0, toBeRemovedTransitionLabelCharIndex));
                    latestNonSoloTransitionPathNode.removeOutgoingTransition(str.charAt(toBeRemovedTransitionLabelCharIndex));
                    transitionCount -= str.substring(toBeRemovedTransitionLabelCharIndex).length();
                    /////

                    replaceOrRegister(sourceNode, str.substring(0, toBeRemovedTransitionLabelCharIndex));
                }

            }
            else
            {
                strEndNode.setAcceptStateStatus(false);
                replaceOrRegister(sourceNode, str);
            }
        }
        else
        {
            unSimplify();
        }
    }


    
    private int calculateMinimizationProcessingStartIndex(String prevStr, String currStr)
    {
        int mpsIndex;

        if (!currStr.startsWith(prevStr))
        {
            //Loop through the corresponding indices of both Strings in search of the first index containing differing characters.
            //The _transition path of the substring of prevStr from this point will need to be submitted for minimization processing.
            //The substring before this point, however, does not, since currStr will simply be extending the right languages of the 
            //nodes on its _transition path.
            int shortestStringLength = Math.min(prevStr.length(), currStr.length());
            for (mpsIndex = 0; mpsIndex < shortestStringLength && prevStr.charAt(mpsIndex) == currStr.charAt(mpsIndex); mpsIndex++)
            {
            }
            ;
            /////
        }
        else
            mpsIndex = -1;    //If the prevStr is a prefix of currStr, then currStr simply extends the right language of the _transition path of prevStr.

        return mpsIndex;
    }


    
    private String determineLongestPrefixInMDAG(String str)
    {
        MDAGNode currentNode = sourceNode;
        int numberOfChars = str.length();
        int onePastPrefixEndIndex = 0;

        //Loop through the characters in str, using them in sequence to _transition
        //through the MDAG until the currently processing node doesn't have a _transition
        //labeled with the current processing char, or there are no more characters to process. 
        for (int i = 0; i < numberOfChars; i++, onePastPrefixEndIndex++)
        {
            char currentChar = str.charAt(i);
            if (currentNode.hasOutgoingTransition(currentChar))
                currentNode = currentNode.transition(currentChar);
            else
                break;
        }
        /////

        return str.substring(0, onePastPrefixEndIndex);
    }


    
    private HashMap<String, Object> getTransitionPathFirstConfluenceNodeData(MDAGNode originNode, String str)
    {
        int currentIndex = 0;
        int charCount = str.length();
        MDAGNode currentNode = originNode;

        //Loop thorugh the characters in str, sequentially using them to _transition through the MDAG in search of
        //(and breaking upon reaching) the first node that is the target of two or more transitions. The loop is 
        //also broken from if the currently processing node doesn't have a _transition labeled with the currently processing char.
        for (; currentIndex < charCount; currentIndex++)
        {
            char currentChar = str.charAt(currentIndex);
            currentNode = (currentNode.hasOutgoingTransition(currentChar) ? currentNode.transition(currentChar) : null);

            if (currentNode == null || currentNode.isConfluenceNode())
                break;
        }
        /////

        boolean noConfluenceNode = (currentNode == originNode || currentIndex == charCount);

        //Create a HashMap containing the index of the last char in the substring corresponding
        //to the transitoin path to the confluence node, as well as the actual confluence node
        HashMap<String, Object> confluenceNodeDataHashMap = new HashMap<String, Object>(2);
        confluenceNodeDataHashMap.put("toConfluenceNodeTransitionCharIndex", (noConfluenceNode ? null : currentIndex));
        confluenceNodeDataHashMap.put("confluenceNode", noConfluenceNode ? null : currentNode);
        /////

        return confluenceNodeDataHashMap;
    }


    
    private void replaceOrRegister(MDAGNode originNode, String str)
    {
        char transitionLabelChar = str.charAt(0);
        MDAGNode relevantTargetNode = originNode.transition(transitionLabelChar);

        //If relevantTargetNode has transitions and there is at least one char left to process, recursively call 
        //this on the next char in order to further processing down the _transition path corresponding to str
        if (relevantTargetNode.hasTransitions() && !str.substring(1).isEmpty())
            replaceOrRegister(relevantTargetNode, str.substring(1));
        /////

        //Get the node representing the equivalence class that relevantTargetNode belongs to. MDAGNodes hash on the
        //transitions paths that can be traversed from them and nodes able to be reached from them;
        //nodes with the same equivalence classes will hash to the same bucket.
        MDAGNode equivalentNode = equivalenceClassMDAGNodeHashMap.get(relevantTargetNode);

        if (equivalentNode == null)  //if there is no node with the same right language as relevantTargetNode
            equivalenceClassMDAGNodeHashMap.put(relevantTargetNode, relevantTargetNode);
        else if (equivalentNode != relevantTargetNode)   //if there is another node with the same right language as relevantTargetNode, reassign the
        {                                               //_transition between originNode and relevantTargetNode, to originNode and the node representing the equivalence class of interest
            relevantTargetNode.decrementTargetIncomingTransitionCounts();
            transitionCount -= relevantTargetNode.getOutgoingTransitionCount(); //Since this method is recursive, the outgoing transitions of all of relevantTargetNode's child nodes have already been reassigned, 
            //so we only need to decrement the _transition count by the relevantTargetNode's outgoing _transition count
            originNode.reassignOutgoingTransition(transitionLabelChar, relevantTargetNode, equivalentNode);
        }
    }


    
    private void addTransitionPath(MDAGNode originNode, String str)
    {
        if (!str.isEmpty())
        {
            MDAGNode currentNode = originNode;
            int charCount = str.length();

            //Loop through the characters in str, iteratevely adding
            // a _transition path corresponding to it from originNode
            for (int i = 0; i < charCount; i++, transitionCount++)
            {
                char currentChar = str.charAt(i);
                boolean isLastChar = (i == charCount - 1);
                currentNode = currentNode.addOutgoingTransition(currentChar, isLastChar);

                charTreeSet.add(currentChar);
            }
            /////
        }
        else
            originNode.setAcceptStateStatus(true);
    }


    
    private void removeTransitionPathRegisterEntries(String str)
    {
        MDAGNode currentNode = sourceNode;

        int charCount = str.length();

        for (int i = 0; i < charCount; i++)
        {
            currentNode = currentNode.transition(str.charAt(i));
            if (equivalenceClassMDAGNodeHashMap.get(currentNode) == currentNode)
                equivalenceClassMDAGNodeHashMap.remove(currentNode);

            //The hashCode of an MDAGNode is cached the first time a hash is performed without a cache value present.
            //Since we just hashed currentNode, we must clear this regardless of its presence in equivalenceClassMDAGNodeHashMap
            //since we're not actually declaring equivalence class representatives here.
            if (currentNode != null) currentNode.clearStoredHashCode();
        }
    }


    
    private void cloneTransitionPath(MDAGNode pivotConfluenceNode, String transitionStringToPivotNode, String str)
    {
        MDAGNode lastTargetNode = pivotConfluenceNode.transition(str);      //Will store the last node which was used as the base of a cloning operation
        MDAGNode lastClonedNode = null;                                     //Will store the last cloned node
        char lastTransitionLabelChar = '\0';                                //Will store the char which labels the _transition to lastTargetNode from its parent node in the prefixString's _transition path

        //Loop backwards through the indices of str, using each as a boundary to create substrings of str of decreasing length
        //which will be used to _transition to, and duplicate the nodes in the _transition path of str from pivotConfluenceNode.
        for (int i = str.length(); i >= 0; i--)
        {
            String currentTransitionString = (i > 0 ? str.substring(0, i) : null);
            MDAGNode currentTargetNode = (i > 0 ? pivotConfluenceNode.transition(currentTransitionString) : pivotConfluenceNode);
            MDAGNode clonedNode;

            if (i == 0)  //if we have reached pivotConfluenceNode
            {
                //Clone pivotConfluenceNode in a way that reassigns the _transition of its parent node (in transitionStringToConfluenceNode's path) to the clone.
                String transitionStringToPivotNodeParent = transitionStringToPivotNode.substring(0, transitionStringToPivotNode.length() - 1);
                char parentTransitionLabelChar = transitionStringToPivotNode.charAt(transitionStringToPivotNode.length() - 1);
                clonedNode = pivotConfluenceNode.clone(sourceNode.transition(transitionStringToPivotNodeParent), parentTransitionLabelChar);
                /////
            }
            else
                clonedNode = currentTargetNode.clone();     //simply clone curentTargetNode

            transitionCount += clonedNode.getOutgoingTransitionCount();

            //If this isn't the first node we've cloned, reassign clonedNode's _transition labeled
            //with the lastTransitionChar (which points to the last targetNode) to the last clone.
            if (lastClonedNode != null)
            {
                clonedNode.reassignOutgoingTransition(lastTransitionLabelChar, lastTargetNode, lastClonedNode);
                lastTargetNode = currentTargetNode;
            }

            //Store clonedNode and the char which labels the _transition between the node it was cloned from (currentTargetNode) and THAT node's parent.
            //These will be used to establish an equivalent _transition to clonedNode from the next clone to be created (it's clone parent).
            lastClonedNode = clonedNode;
            lastTransitionLabelChar = (i > 0 ? str.charAt(i - 1) : '\0');
            /////
        }
        /////
    }


    
    private void addStringInternal(String str)
    {
        String prefixString = determineLongestPrefixInMDAG(str);
        String suffixString = str.substring(prefixString.length());

        //Retrive the data related to the first confluence node (a node with two or more incoming transitions)
        //in the _transition path from sourceNode corresponding to prefixString.
        HashMap<String, Object> firstConfluenceNodeDataHashMap = getTransitionPathFirstConfluenceNodeData(sourceNode, prefixString);
        MDAGNode firstConfluenceNodeInPrefix = (MDAGNode) firstConfluenceNodeDataHashMap.get("confluenceNode");
        Integer toFirstConfluenceNodeTransitionCharIndex = (Integer) firstConfluenceNodeDataHashMap.get("toConfluenceNodeTransitionCharIndex");
        /////

        //Remove the register entries of all the nodes in the prefixString _transition path up to the first confluence node
        //(those past the confluence node will not need to be removed since they will be cloned and unaffected by the 
        //addition of suffixString). If there is no confluence node in prefixString, then remove the register entries in prefixString's entire _transition path
        removeTransitionPathRegisterEntries((toFirstConfluenceNodeTransitionCharIndex == null ? prefixString : prefixString.substring(0, toFirstConfluenceNodeTransitionCharIndex)));

        //If there is a confluence node in the prefix, we must duplicate the _transition path
        //of the prefix starting from that node, before we add suffixString (to the duplicate path).
        //This ensures that we do not disturb the other _transition paths containing this node.
        if (firstConfluenceNodeInPrefix != null)
        {
            String transitionStringOfPathToFirstConfluenceNode = prefixString.substring(0, toFirstConfluenceNodeTransitionCharIndex + 1);
            String transitionStringOfToBeDuplicatedPath = prefixString.substring(toFirstConfluenceNodeTransitionCharIndex + 1);
            cloneTransitionPath(firstConfluenceNodeInPrefix, transitionStringOfPathToFirstConfluenceNode, transitionStringOfToBeDuplicatedPath);
        }
        /////

        //Add the _transition based on suffixString to the end of the (possibly duplicated) _transition path corresponding to prefixString
        addTransitionPath(sourceNode.transition(prefixString), suffixString);
    }


    
    private int createSimpleMDAGTransitionSet(MDAGNode node, SimpleMDAGNode[] mdagDataArray, int onePastLastCreatedTransitionSetIndex)
    {
        int pivotIndex = onePastLastCreatedTransitionSetIndex;  // node自己的位置
        node.setTransitionSetBeginIndex(pivotIndex);

        onePastLastCreatedTransitionSetIndex += node.getOutgoingTransitionCount();  // 这个参数代表id的消耗

        //Create a SimpleMDAGNode representing each _transition label/target combo in transitionTreeMap, recursively calling this method (if necessary)
        //to set indices in these SimpleMDAGNodes that the set of transitions emitting from their respective _transition targets starts from.
        TreeMap<Character, MDAGNode> transitionTreeMap = node.getOutgoingTransitions();
        for (Entry<Character, MDAGNode> transitionKeyValuePair : transitionTreeMap.entrySet())
        {
            //Use the current _transition's label and target node to create a SimpleMDAGNode
            //(which is a space-saving representation of the _transition), and insert it in to mdagDataArray
            char transitionLabelChar = transitionKeyValuePair.getKey();
            MDAGNode transitionTargetNode = transitionKeyValuePair.getValue();
            mdagDataArray[pivotIndex] = new SimpleMDAGNode(transitionLabelChar, transitionTargetNode.isAcceptNode(), transitionTargetNode.getOutgoingTransitionCount());
            /////

            //If targetTransitionNode's outgoing _transition set hasn't been inserted in to mdagDataArray yet, call this method on it to do so.
            //After this call returns, transitionTargetNode will contain the index in mdagDataArray that its _transition set starts from
            if (transitionTargetNode.getTransitionSetBeginIndex() == -1)
                onePastLastCreatedTransitionSetIndex = createSimpleMDAGTransitionSet(transitionTargetNode, mdagDataArray, onePastLastCreatedTransitionSetIndex);

            mdagDataArray[pivotIndex++].setTransitionSetBeginIndex(transitionTargetNode.getTransitionSetBeginIndex());
        }
        /////

        return onePastLastCreatedTransitionSetIndex;
    }


    
    public void simplify()
    {
        if (sourceNode != null)
        {
            mdagDataArray = new SimpleMDAGNode[transitionCount];
            createSimpleMDAGTransitionSet(sourceNode, mdagDataArray, 0);
            simplifiedSourceNode = new SimpleMDAGNode('\0', false, sourceNode.getOutgoingTransitionCount());

            //Mark the previous MDAG data structure and equivalenceClassMDAGNodeHashMap
            //for garbage collection since they are no longer needed.
            sourceNode = null;
            equivalenceClassMDAGNodeHashMap = null;
            /////
        }
    }

    
    public void unSimplify()
    {
        if (sourceNode == null)
        {
            sourceNode = new MDAGNode(false);
            equivalenceClassMDAGNodeHashMap = new HashMap<MDAGNode, MDAGNode>();
            MDAGNode[] toNodeArray = new MDAGNode[mdagDataArray.length];
            createMDAGNode(simplifiedSourceNode, -1, toNodeArray, new MDAGNode[mdagDataArray.length]);
            // 构建注册表
            for (MDAGNode mdagNode : toNodeArray)
            {
                equivalenceClassMDAGNodeHashMap.put(mdagNode, mdagNode);
            }
            // 扔掉垃圾
            simplifiedSourceNode = null;
        }
    }

    
    private void createMDAGNode(SimpleMDAGNode current, int fromIndex, MDAGNode[] toNodeArray, MDAGNode[] fromNodeArray)
    {
        MDAGNode from = (fromIndex == -1 ? sourceNode : toNodeArray[fromIndex]);
        int transitionSetBegin = current.getTransitionSetBeginIndex();
        int onePastTransitionSetEnd = transitionSetBegin + current.getOutgoingTransitionSetSize();

        for (int i = transitionSetBegin; i < onePastTransitionSetEnd; i++)
        {
            SimpleMDAGNode targetNode = mdagDataArray[i];
            if (toNodeArray[i] != null)
            {
                fromNodeArray[fromIndex].addOutgoingTransition(current.getLetter(), fromNodeArray[i]);
                toNodeArray[fromIndex] = fromNodeArray[i];
                continue;
            }
            toNodeArray[i] = from.addOutgoingTransition(targetNode.getLetter(), targetNode.isAcceptNode());
            fromNodeArray[i] = from;
            createMDAGNode(targetNode, i, toNodeArray, fromNodeArray);
        }
    }



    
    public boolean contains(String str)
    {
        if (sourceNode != null)      //if the MDAG hasn't been simplified
        {
            MDAGNode targetNode = sourceNode.transition(str.toCharArray());
            return (targetNode != null && targetNode.isAcceptNode());
        }
        else
        {
            SimpleMDAGNode targetNode = simplifiedSourceNode.transition(mdagDataArray, str.toCharArray());
            return (targetNode != null && targetNode.isAcceptNode());
        }
    }


    
    private void getStrings(HashSet<String> strHashSet, SearchCondition searchCondition, String searchConditionString, String prefixString, TreeMap<Character, MDAGNode> transitionTreeMap)
    {
        //Traverse all the valid _transition paths beginning from each _transition in transitionTreeMap, inserting the
        //corresponding Strings in to strHashSet that have the relationship with conditionString denoted by searchCondition
        for (Entry<Character, MDAGNode> transitionKeyValuePair : transitionTreeMap.entrySet())
        {
            String newPrefixString = prefixString + transitionKeyValuePair.getKey();
            MDAGNode currentNode = transitionKeyValuePair.getValue();

            if (currentNode.isAcceptNode() && searchCondition.satisfiesCondition(newPrefixString, searchConditionString))
                strHashSet.add(newPrefixString);

            //Recursively call this to traverse all the valid _transition paths from currentNode
            getStrings(strHashSet, searchCondition, searchConditionString, newPrefixString, currentNode.getOutgoingTransitions());
        }
        /////
    }


    
    private void getStrings(HashSet<String> strHashSet, SearchCondition searchCondition, String searchConditionString, String prefixString, SimpleMDAGNode node)
    {
        int transitionSetBegin = node.getTransitionSetBeginIndex();
        int onePastTransitionSetEnd = transitionSetBegin + node.getOutgoingTransitionSetSize();

        //Traverse all the valid _transition paths beginning from each _transition in transitionTreeMap, inserting the
        //corresponding Strings in to strHashSet that have the relationship with conditionString denoted by searchCondition
        for (int i = transitionSetBegin; i < onePastTransitionSetEnd; i++)
        {
            SimpleMDAGNode currentNode = mdagDataArray[i];
            String newPrefixString = prefixString + currentNode.getLetter();

            if (currentNode.isAcceptNode() && searchCondition.satisfiesCondition(newPrefixString, searchConditionString))
                strHashSet.add(newPrefixString);

            //Recursively call this to traverse all the valid _transition paths from currentNode
            getStrings(strHashSet, searchCondition, searchConditionString, newPrefixString, currentNode);
        }
        /////
    }


    
    public HashSet<String> getAllStrings()
    {
        HashSet<String> strHashSet = new LinkedHashSet<String>();

        if (sourceNode != null)
            getStrings(strHashSet, SearchCondition.NO_SEARCH_CONDITION, null, "", sourceNode.getOutgoingTransitions());
        else
            getStrings(strHashSet, SearchCondition.NO_SEARCH_CONDITION, null, "", simplifiedSourceNode);

        return strHashSet;
    }


    
    public HashSet<String> getStringsStartingWith(String prefixStr)
    {
        HashSet<String> strHashSet = new HashSet<String>();

        if (sourceNode != null)      //if the MDAG hasn't been simplified
        {
            MDAGNode originNode = sourceNode.transition(prefixStr);  //attempt to _transition down the path denoted by prefixStr

            if (originNode != null) //if there a _transition path corresponding to prefixString (one or more stored Strings begin with prefixString)
            {
                if (originNode.isAcceptNode()) strHashSet.add(prefixStr);
                getStrings(strHashSet, SearchCondition.PREFIX_SEARCH_CONDITION, prefixStr, prefixStr, originNode.getOutgoingTransitions());   //retrieve all Strings that extend the _transition path denoted by prefixStr
            }
        }
        else
        {
            SimpleMDAGNode originNode = SimpleMDAGNode.traverseMDAG(mdagDataArray, simplifiedSourceNode, prefixStr);      //attempt to _transition down the path denoted by prefixStr

            if (originNode != null)      //if there a _transition path corresponding to prefixString (one or more stored Strings begin with prefixStr)
            {
                if (originNode.isAcceptNode()) strHashSet.add(prefixStr);
                getStrings(strHashSet, SearchCondition.PREFIX_SEARCH_CONDITION, prefixStr, prefixStr, originNode);        //retrieve all Strings that extend the _transition path denoted by prefixString
            }
        }

        return strHashSet;
    }


    
    public HashSet<String> getStringsWithSubstring(String str)
    {
        HashSet<String> strHashSet = new HashSet<String>();

        if (sourceNode != null)      //if the MDAG hasn't been simplified
            getStrings(strHashSet, SearchCondition.SUBSTRING_SEARCH_CONDITION, str, "", sourceNode.getOutgoingTransitions());
        else
            getStrings(strHashSet, SearchCondition.SUBSTRING_SEARCH_CONDITION, str, "", simplifiedSourceNode);

        return strHashSet;
    }


    
    public HashSet<String> getStringsEndingWith(String suffixStr)
    {
        HashSet<String> strHashSet = new HashSet<String>();

        if (sourceNode != null)      //if the MDAG hasn't been simplified
            getStrings(strHashSet, SearchCondition.SUFFIX_SEARCH_CONDITION, suffixStr, "", sourceNode.getOutgoingTransitions());
        else
            getStrings(strHashSet, SearchCondition.SUFFIX_SEARCH_CONDITION, suffixStr, "", simplifiedSourceNode);

        return strHashSet;
    }


    
    private Object getSourceNode()
    {
        return (sourceNode != null ? sourceNode : simplifiedSourceNode);
    }


    
    public SimpleMDAGNode[] getSimpleMDAGArray()
    {
        return mdagDataArray;
    }


    
    private TreeSet<Character> getTransitionLabelSet()
    {
        return charTreeSet;
    }


    
    private static boolean isAcceptNode(Object nodeObj)
    {
        if (nodeObj != null)
        {
            Class nodeObjClass = nodeObj.getClass();

            if (nodeObjClass.equals(MDAGNode.class))
                return ((MDAGNode) nodeObj).isAcceptNode();
            else if (nodeObjClass.equals(SimpleMDAGNode.class))
                return ((SimpleMDAGNode) nodeObj).isAcceptNode();

        }

        throw new IllegalArgumentException("Argument is not an MDAGNode or SimpleMDAGNode");
    }

//    @Override
//    public String toString()
//    {
//        final StringBuilder sb = new StringBuilder("MDAG{");
//        sb.append("sourceNode=").append(sourceNode);
//        sb.append(", simplifiedSourceNode=").append(simplifiedSourceNode);
//        sb.append(", equivalenceClassMDAGNodeHashMap=").append(equivalenceClassMDAGNodeHashMap);
//        sb.append(", mdagDataArray=").append(Arrays.toString(mdagDataArray));
//        sb.append(", charTreeSet=").append(charTreeSet);
//        sb.append(", transitionCount=").append(transitionCount);
//        sb.append('}');
//        return sb.toString();
//    }

    
    public HashMap<MDAGNode, MDAGNode> _getEquivalenceClassMDAGNodeHashMap()
    {
        return new HashMap<MDAGNode, MDAGNode>(equivalenceClassMDAGNodeHashMap);
    }
}
