
package in.thyferny.nlp.algoritm;

import java.util.List;

import in.thyferny.nlp.dictionary.common.CommonSynonymDictionary;


public class EditDistance
{
    public static long compute(List<CommonSynonymDictionary.SynonymItem> synonymItemListA, List<CommonSynonymDictionary.SynonymItem> synonymItemListB)
    {
        long[] arrayA = new long[synonymItemListA.size()];
        long[] arrayB = new long[synonymItemListB.size()];
        int i = 0;
        for (CommonSynonymDictionary.SynonymItem item : synonymItemListA)
        {
            arrayA[i++] = item.entry.id;
        }
        i = 0;
        for (CommonSynonymDictionary.SynonymItem item : synonymItemListB)
        {
            arrayB[i++] = item.entry.id;
        }
        return compute(arrayA, arrayB);
    }

    public static long compute(long[] arrayA, long[] arrayB)
    {
        final int m = arrayA.length;
        final int n = arrayB.length;
        if (m == 0 || n == 0) return Long.MAX_VALUE / 3;

        long[][] d = new long[m + 1][n + 1];
        for (int j = 0; j <= n; ++j)
        {
            d[0][j] = j;
        }
        for (int i = 0; i <= m; ++i)
        {
            d[i][0] = i;
        }

        for (int i = 1; i <= m; ++i)
        {
            long ci = arrayA[i - 1];
            for (int j = 1; j <= n; ++j)
            {
                long cj = arrayB[j - 1];
                if (ci == cj)
                {
                    d[i][j] = d[i - 1][j - 1];
                }
//                else if (i > 1 && j > 1 && ci == arrayA[j - 2] && cj == arrayB[i - 2])
//                {
//                    // 交错相等
//                    d[i][j] = 1 + Math.min(d[i - 2][j - 2], Math.min(d[i][j - 1], d[i - 1][j]));
//                }
                else
                {
                    // 等号右边的分别代表 将ci改成cj                                    错串加cj         错串删ci
                    d[i][j] = Math.min(d[i - 1][j - 1] + Math.abs(ci - cj), Math.min(d[i][j - 1] + cj, d[i - 1][j] + ci));
                }
            }
        }

        return d[m][n];
    }

    public static int compute(int[] arrayA, int[] arrayB)
    {
        final int m = arrayA.length;
        final int n = arrayB.length;
        if (m == 0 || n == 0) return Integer.MAX_VALUE / 3;

        int[][] d = new int[m + 1][n + 1];
        for (int j = 0; j <= n; ++j)
        {
            d[0][j] = j;
        }
        for (int i = 0; i <= m; ++i)
        {
            d[i][0] = i;
        }

        for (int i = 1; i <= m; ++i)
        {
            int ci = arrayA[i - 1];
            for (int j = 1; j <= n; ++j)
            {
                int cj = arrayB[j - 1];
                if (ci == cj)
                {
                    d[i][j] = d[i - 1][j - 1];
                }
//                else if (i > 1 && j > 1 && ci == arrayA[j - 2] && cj == arrayB[i - 2])
//                {
//                    // 交错相等
//                    d[i][j] = 1 + Math.min(d[i - 2][j - 2], Math.min(d[i][j - 1], d[i - 1][j]));
//                }
                else
                {
                    // 等号右边的分别代表 将ci改成cj                                    错串加cj         错串删ci
                    d[i][j] = Math.min(d[i - 1][j - 1] + Math.abs(ci - cj), Math.min(d[i][j - 1] + cj, d[i - 1][j] + ci));
                }
            }
        }

        return d[m][n];
    }

    
    public static int ed(String wrongWord, String rightWord)
    {
        final int m = wrongWord.length();
        final int n = rightWord.length();

        int[][] d = new int[m + 1][n + 1];
        for (int j = 0; j <= n; ++j)
        {
            d[0][j] = j;
        }
        for (int i = 0; i <= m; ++i)
        {
            d[i][0] = i;
        }

        for (int i = 1; i <= m; ++i)
        {
            char ci = wrongWord.charAt(i - 1);
            for (int j = 1; j <= n; ++j)
            {
                char cj = rightWord.charAt(j - 1);
                if (ci == cj)
                {
                    d[i][j] = d[i - 1][j - 1];
                }
                else if (i > 1 && j > 1 && ci == rightWord.charAt(j - 2) && cj == wrongWord.charAt(i - 2))
                {
                    // 交错相等
                    d[i][j] = 1 + Math.min(d[i - 2][j - 2], Math.min(d[i][j - 1], d[i - 1][j]));
                }
                else
                {
                    // 等号右边的分别代表 将ci改成cj                   错串加cj         错串删ci
                    d[i][j] = Math.min(d[i - 1][j - 1] + 1, Math.min(d[i][j - 1] + 1, d[i - 1][j] + 1));
                }
            }
        }

        return d[m][n];
    }

    
    public static int compute(char[] wrongWord, char[] rightWord)
    {
        final int m = wrongWord.length;
        final int n = rightWord.length;

        int[][] d = new int[m + 1][n + 1];
        for (int j = 0; j <= n; ++j)
        {
            d[0][j] = j;
        }
        for (int i = 0; i <= m; ++i)
        {
            d[i][0] = i;
        }

        for (int i = 1; i <= m; ++i)
        {
            char ci = wrongWord[i - 1];
            for (int j = 1; j <= n; ++j)
            {
                char cj = rightWord[j - 1];
                if (ci == cj)
                {
                    d[i][j] = d[i - 1][j - 1];
                }
                else if (i > 1 && j > 1 && ci == rightWord[j - 2] && cj == wrongWord[i - 2])
                {
                    // 交错相等
                    d[i][j] = 1 + Math.min(d[i - 2][j - 2], Math.min(d[i][j - 1], d[i - 1][j]));
                }
                else
                {
                    // 等号右边的分别代表 将ci改成cj                   错串加cj         错串删ci
                    d[i][j] = Math.min(d[i - 1][j - 1] + 1, Math.min(d[i][j - 1] + 1, d[i - 1][j] + 1));
                }
            }
        }

        return d[m][n];
    }
}
