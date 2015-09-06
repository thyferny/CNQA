

package in.thyferny.nlp.model.maxent;


public class UniformPrior
{
    private int numOutcomes;
    private double r;

    
    public void logPrior(double[] dist)
    {
        for (int oi = 0; oi < numOutcomes; oi++)
        {
            dist[oi] = r;
        }
    }

    
    public void setLabels(String[] outcomeLabels)
    {
        this.numOutcomes = outcomeLabels.length;
        r = Math.log(1.0 / numOutcomes);
    }
}
