package com.emistoolbox.common.results;

import com.emistoolbox.common.results.impl.ResultImpl;

public class ResultUtil
{
    public static int[] getIndexes(String[][] headers, String[] indexes)
    {
        int[] result = new int[indexes.length];
        for (int i = 0; i < indexes.length; i++)
        {
            result[i] = getIndex(headers[i], indexes[i]);
            if (result[i] == -1)
            {
                throw new IllegalArgumentException("Failed to find index for '" + indexes[i] + "' in dimension " + i);
            }
        }
        return result;
    }

    public static int getIndex(String[] headers, String index)
    {
        for (int i = 0; i < headers.length; i++)
        {
            if (headers[i].equals(index))
                return i;
        }
        return -1;
    }

    public static Result mergeResults(Result[] results)
    {
        int[] dims = new int[results[0].getDimensions() + 1];
        for (int i = 0; i < results[0].getDimensions(); i++)
            dims[i] = results[0].getDimensionSize(i);
        dims[(dims.length - 1)] = results.length;

        ResultImpl mergeResult = new ResultImpl(dims);
        mergeResult.merge(results);

        return mergeResult;
    }

    public static double add(double value1, double value2)
    {
        if (Double.isNaN(value1))
            return value2;
        if (Double.isNaN(value2))
            return value1;

        return value1 + value2;
    }
}
