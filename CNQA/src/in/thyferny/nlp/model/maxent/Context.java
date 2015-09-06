

package in.thyferny.nlp.model.maxent;


public class Context
{

    
    protected double[] parameters;
    
    protected int[] outcomes;

    
    public Context(int[] outcomePattern, double[] parameters)
    {
        this.outcomes = outcomePattern;
        this.parameters = parameters;
    }

    
    public int[] getOutcomes()
    {
        return outcomes;
    }

    
    public double[] getParameters()
    {
        return parameters;
    }
}
