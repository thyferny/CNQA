package in.thyferny.nlp.algoritm.ahocorasick.trie;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import in.thyferny.nlp.algoritm.ahocorasick.interval.IntervalTree;
import in.thyferny.nlp.algoritm.ahocorasick.interval.Intervalable;


public class Trie
{

    private TrieConfig trieConfig;

    private State rootState;

    
    private boolean failureStatesConstructed = false;

    
    public Trie(TrieConfig trieConfig)
    {
        this.trieConfig = trieConfig;
        this.rootState = new State();
    }

    public Trie()
    {
        this(new TrieConfig());
    }


    public Trie removeOverlaps()
    {
        this.trieConfig.setAllowOverlaps(false);
        return this;
    }

    
    public Trie remainLongest()
    {
        this.trieConfig.remainLongest = true;
        return this;
    }

    public void addKeyword(String keyword)
    {
        if (keyword == null || keyword.length() == 0)
        {
            return;
        }
        State currentState = this.rootState;
        for (Character character : keyword.toCharArray())
        {
            currentState = currentState.addState(character);
        }
        currentState.addEmit(keyword);
    }

    public void addAllKeyword(Collection<String> keywordSet)
    {
        for (String keyword : keywordSet)
        {
            addKeyword(keyword);
        }
    }

    
    public Collection<Token> tokenize(String text)
    {

        Collection<Token> tokens = new ArrayList<Token>();

        Collection<Emit> collectedEmits = parseText(text);
        // 下面是最长分词的关键
        IntervalTree intervalTree = new IntervalTree((List<Intervalable>) (List<?>) collectedEmits);
        intervalTree.removeOverlaps((List<Intervalable>) (List<?>) collectedEmits);
        // 移除结束

        int lastCollectedPosition = -1;
        for (Emit emit : collectedEmits)
        {
            if (emit.getStart() - lastCollectedPosition > 1)
            {
                tokens.add(createFragment(emit, text, lastCollectedPosition));
            }
            tokens.add(createMatch(emit, text));
            lastCollectedPosition = emit.getEnd();
        }
        if (text.length() - lastCollectedPosition > 1)
        {
            tokens.add(createFragment(null, text, lastCollectedPosition));
        }

        return tokens;
    }

    private Token createFragment(Emit emit, String text, int lastCollectedPosition)
    {
        return new FragmentToken(text.substring(lastCollectedPosition + 1, emit == null ? text.length() : emit.getStart()));
    }

    private Token createMatch(Emit emit, String text)
    {
        return new MatchToken(text.substring(emit.getStart(), emit.getEnd() + 1), emit);
    }

    
    @SuppressWarnings("unchecked")
    public Collection<Emit> parseText(String text)
    {
        checkForConstructedFailureStates();

        int position = 0;
        State currentState = this.rootState;
        List<Emit> collectedEmits = new ArrayList<Emit>();
        for (int i = 0; i < text.length(); ++i)
        {
            currentState = getState(currentState, text.charAt(i));
            storeEmits(position, currentState, collectedEmits);
            ++position;
        }

        if (!trieConfig.isAllowOverlaps())
        {
            IntervalTree intervalTree = new IntervalTree((List<Intervalable>) (List<?>) collectedEmits);
            intervalTree.removeOverlaps((List<Intervalable>) (List<?>) collectedEmits);
        }

        if (trieConfig.remainLongest)
        {
            remainLongest(collectedEmits);
        }

        return collectedEmits;
    }

    
    private static void remainLongest(Collection<Emit> collectedEmits)
    {
        if (collectedEmits.size() < 2) return;
        Map<Integer, Emit> emitMapStart = new TreeMap<Integer, Emit>();
        for (Emit emit : collectedEmits)
        {
            Emit pre = emitMapStart.get(emit.getStart());
            if (pre == null || pre.size() < emit.size())
            {
                emitMapStart.put(emit.getStart(), emit);
            }
        }
        if (emitMapStart.size() < 2)
        {
            collectedEmits.clear();
            collectedEmits.addAll(emitMapStart.values());
            return;
        }
        Map<Integer, Emit> emitMapEnd = new TreeMap<Integer, Emit>();
        for (Emit emit : emitMapStart.values())
        {
            Emit pre = emitMapEnd.get(emit.getEnd());
            if (pre == null || pre.size() < emit.size())
            {
                emitMapEnd.put(emit.getEnd(), emit);
            }
        }

        collectedEmits.clear();
        collectedEmits.addAll(emitMapEnd.values());
    }


    
    private static State getState(State currentState, Character character)
    {
        State newCurrentState = currentState.nextState(character);  // 先按success跳转
        while (newCurrentState == null) // 跳转失败的话，按failure跳转
        {
            currentState = currentState.failure();
            newCurrentState = currentState.nextState(character);
        }
        return newCurrentState;
    }

    
    private void checkForConstructedFailureStates()
    {
        if (!this.failureStatesConstructed)
        {
            constructFailureStates();
        }
    }

    
    private void constructFailureStates()
    {
        Queue<State> queue = new LinkedBlockingDeque<State>();

        // 第一步，将深度为1的节点的failure设为根节点
        for (State depthOneState : this.rootState.getStates())
        {
            depthOneState.setFailure(this.rootState);
            queue.add(depthOneState);
        }
        this.failureStatesConstructed = true;

        // 第二步，为深度 > 1 的节点建立failure表，这是一个bfs
        while (!queue.isEmpty())
        {
            State currentState = queue.remove();

            for (Character transition : currentState.getTransitions())
            {
                State targetState = currentState.nextState(transition);
                queue.add(targetState);

                State traceFailureState = currentState.failure();
                while (traceFailureState.nextState(transition) == null)
                {
                    traceFailureState = traceFailureState.failure();
                }
                State newFailureState = traceFailureState.nextState(transition);
                targetState.setFailure(newFailureState);
                targetState.addEmit(newFailureState.emit());
            }
        }
    }

    public void dfs(IWalker walker)
    {
        checkForConstructedFailureStates();
        dfs(rootState, "", walker);
    }

    private void dfs(State currentState, String path, IWalker walker)
    {
        walker.meet(path, currentState);
        for (Character transition : currentState.getTransitions())
        {
            State targetState = currentState.nextState(transition);
            dfs(targetState, path + transition, walker);
        }
    }


    public static interface IWalker
    {
        
        void meet(String path, State state);
    }

    
    private static void storeEmits(int position, State currentState, List<Emit> collectedEmits)
    {
        Collection<String> emits = currentState.emit();
        if (emits != null && !emits.isEmpty())
        {
            for (String emit : emits)
            {
                collectedEmits.add(new Emit(position - emit.length() + 1, position, emit));
            }
        }
    }

}
