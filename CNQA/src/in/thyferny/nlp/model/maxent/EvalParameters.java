

package in.thyferny.nlp.model.maxent;


public class EvalParameters
{

    
    private Context[] params;
    
    private final int numOutcomes;
    
    private double correctionConstant;

    
    private final double constantInverse;
    
    private double correctionParam;

    
    public EvalParameters(Context[] params, double correctionParam, double correctionConstant, int numOutcomes)
    {
        this.params = params;
        this.correctionParam = correctionParam;
        this.numOutcomes = numOutcomes;
        this.correctionConstant = correctionConstant;
        this.constantInverse = 1.0 / correctionConstant;
    }

    public EvalParameters(Context[] params, int numOutcomes)
    {
        this(params, 0, 0, numOutcomes);
    }

    public Context[] getParams()
    {
        return params;
    }

    public int getNumOutcomes()
    {
        return numOutcomes;
    }

    public double getCorrectionConstant()
    {
        return correctionConstant;
    }

    public double getConstantInverse()
    {
        return constantInverse;
    }

    public double getCorrectionParam()
    {
        return correctionParam;
    }

    public void setCorrectionParam(double correctionParam)
    {
        this.correctionParam = correctionParam;
    }
}