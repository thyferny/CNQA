package in.thyferny.nlp.algoritm.ahocorasick.trie;

import java.util.*;


public class State
{

    
    protected final int depth;

    
    private State failure = null;

    
    private Set<String> emits = null;
    
    private Map<Character, State> success = new TreeMap<Character, State>();

    
    public State()
    {
        this(0);
    }

    
    public State(int depth)
    {
        this.depth = depth;
    }

    
    public int getDepth()
    {
        return this.depth;
    }

    
    public void addEmit(String keyword)
    {
        if (this.emits == null)
        {
            this.emits = new TreeSet<String>();
        }
        this.emits.add(keyword);
    }

    
    public void addEmit(Collection<String> emits)
    {
        for (String emit : emits)
        {
            addEmit(emit);
        }
    }

    
    public Collection<String> emit()
    {
        return this.emits == null ? Collections.<String>emptyList() : this.emits;
    }

    
    public State failure()
    {
        return this.failure;
    }

    
    public void setFailure(State failState)
    {
        this.failure = failState;
    }

    
    private State nextState(Character character, boolean ignoreRootState)
    {
        State nextState = this.success.get(character);
        if (!ignoreRootState && nextState == null && this.depth == 0)
        {
            nextState = this;
        }
        return nextState;
    }

    
    public State nextState(Character character)
    {
        return nextState(character, false);
    }

    
    public State nextStateIgnoreRootState(Character character)
    {
        return nextState(character, true);
    }

    public State addState(Character character)
    {
        State nextState = nextStateIgnoreRootState(character);
        if (nextState == null)
        {
            nextState = new State(this.depth + 1);
            this.success.put(character, nextState);
        }
        return nextState;
    }

    public Collection<State> getStates()
    {
        return this.success.values();
    }

    public Collection<Character> getTransitions()
    {
        return this.success.keySet();
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("State{");
        sb.append("depth=").append(depth);
        sb.append(", emits=").append(emits);
        sb.append(", success=").append(success.keySet());
        sb.append(", failure=").append(failure);
        sb.append('}');
        return sb.toString();
    }
}
