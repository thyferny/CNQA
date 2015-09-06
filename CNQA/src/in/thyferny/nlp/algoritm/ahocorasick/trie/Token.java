package in.thyferny.nlp.algoritm.ahocorasick.trie;


public abstract class Token
{
    
    private String fragment;

    public Token(String fragment)
    {
        this.fragment = fragment;
    }

    public String getFragment()
    {
        return this.fragment;
    }

    public abstract boolean isMatch();

    public abstract Emit getEmit();

    @Override
    public String toString()
    {
        return fragment + "/" + isMatch();
    }
}
