
package in.thyferny.nlp.dictionary;

import static in.thyferny.nlp.utility.Predefine.logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;


public class TransformMatrixDictionary<E extends Enum<E>>
{
    Class<E> enumType;
    
    public int ordinaryMax;

    public TransformMatrixDictionary(Class<E> enumType)
    {
        this.enumType = enumType;
    }

    
    int matrix[][];

    
    int total[];

    
    int totalFrequency;

    // HMM的五元组
    
    public int[] states;
    //int[] observations;
    
    public double[] start_probability;
    
    public double[][] transititon_probability;

    public boolean load(String path)
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            // 第一行是矩阵的各个类型
            String line = br.readLine();
            String[] _param = line.split(",");
            // 为了制表方便，第一个label是废物，所以要抹掉它
            String[] labels = new String[_param.length - 1];
            System.arraycopy(_param, 1, labels, 0, labels.length);
            int[] ordinaryArray = new int[labels.length];
            ordinaryMax = 0;
            for (int i = 0; i < ordinaryArray.length; ++i)
            {
                ordinaryArray[i] = convert(labels[i]).ordinal();
                ordinaryMax = Math.max(ordinaryMax, ordinaryArray[i]);
            }
            ++ordinaryMax;
            matrix = new int[ordinaryMax][ordinaryMax];
            for (int i = 0; i < ordinaryMax; ++i)
            {
                for (int j = 0; j < ordinaryMax; ++j)
                {
                    matrix[i][j] = 0;
                }
            }
            // 之后就描述了矩阵
            while ((line = br.readLine()) != null)
            {
                String[] paramArray = line.split(",");
                int currentOrdinary = convert(paramArray[0]).ordinal();
                for (int i = 0; i < ordinaryArray.length; ++i)
                {
                    matrix[currentOrdinary][ordinaryArray[i]] = Integer.valueOf(paramArray[1 + i]);
                }
            }
            br.close();
            // 需要统计一下每个标签出现的次数
            total = new int[ordinaryMax];
            for (int j = 0; j < ordinaryMax; ++j)
            {
                total[j] = 0;
                for (int i = 0; i < ordinaryMax; ++i)
                {
                    total[j] += matrix[i][j];
                }
            }
            for (int j = 0; j < ordinaryMax; ++j)
            {
                total[j] += matrix[j][j];
            }
            for (int j = 0; j < ordinaryMax; ++j)
            {
                totalFrequency += total[j];
            }
            // 下面计算HMM四元组
            states = ordinaryArray;
            start_probability = new double[ordinaryMax];
            for (int s : states)
            {
                double frequency = total[s] + 1e-8;
                start_probability[s] = -Math.log(frequency / totalFrequency);
            }
            transititon_probability = new double[ordinaryMax][ordinaryMax];
            for (int from : states)
            {
                for (int to : states)
                {
                    double frequency = matrix[from][to] + 1e-8;
                    transititon_probability[from][to] = -Math.log(frequency / totalFrequency);
//                    System.out.println("from" + NR.values()[from] + " to" + NR.values()[to] + " = " + transititon_probability[from][to]);
                }
            }
        }
        catch (Exception e)
        {
            logger.warning("读取" + path + "失败" + e);
        }

        return true;
    }

    
    public int getFrequency(String from, String to)
    {
        return getFrequency(convert(from), convert(to));
    }

    
    public int getFrequency(E from, E to)
    {
        return matrix[from.ordinal()][to.ordinal()];
    }

    
    public int getTotalFrequency(E e)
    {
        return total[e.ordinal()];
    }

    
    public int getTotalFrequency()
    {
        return totalFrequency;
    }

    protected E convert(String label)
    {
        return Enum.valueOf(enumType, label);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("TransformMatrixDictionary{");
        sb.append("enumType=").append(enumType);
        sb.append(", ordinaryMax=").append(ordinaryMax);
        sb.append(", matrix=").append(Arrays.toString(matrix));
        sb.append(", total=").append(Arrays.toString(total));
        sb.append(", totalFrequency=").append(totalFrequency);
        sb.append('}');
        return sb.toString();
    }
}
