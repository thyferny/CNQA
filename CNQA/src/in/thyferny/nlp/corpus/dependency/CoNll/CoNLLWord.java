
package in.thyferny.nlp.corpus.dependency.CoNll;


public class CoNLLWord
{
    
    public int ID;
    
    public String LEMMA;
    
    public String CPOSTAG;
    
    public String POSTAG;
    
    public CoNLLWord HEAD;
    
    public String DEPREL;

    
    public String NAME;

    
    public static final CoNLLWord ROOT = new CoNLLWord(0, "##核心##", "ROOT", "root");
    
    public static final CoNLLWord NULL = new CoNLLWord(-1, "##空白##", "NULL", "null");

    
    public CoNLLWord(int ID, String LEMMA, String POSTAG)
    {
        this.ID = ID;
        this.LEMMA = LEMMA;
        this.CPOSTAG = POSTAG.substring(0, 1);   // 取首字母作为粗粒度词性
        this.POSTAG = POSTAG;
        compile();
    }

    
    public CoNLLWord(int ID, String LEMMA, String CPOSTAG, String POSTAG)
    {
        this.ID = ID;
        this.LEMMA = LEMMA;
        this.CPOSTAG = CPOSTAG;
        this.POSTAG = POSTAG;
        compile();
    }

    private void compile()
    {
        this.NAME = PosTagCompiler.compile(POSTAG, LEMMA);
    }

    public CoNLLWord(CoNllLine line)
    {
        LEMMA = line.value[2];
        CPOSTAG = line.value[3];
        POSTAG = line.value[4];
        DEPREL = line.value[7];
        ID = line.id;
        compile();
    }

    public CoNLLWord(CoNllLine[] lineArray, int index)
    {
        this(lineArray[index]);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(ID).append('\t').append(LEMMA).append('\t').append(LEMMA).append('\t').append(CPOSTAG).append('\t')
                .append(POSTAG).append('\t').append('_').append('\t').append(HEAD.ID).append('\t').append(DEPREL).append('\t')
                .append('_').append('\t').append('_');
        return sb.toString();
    }
}
