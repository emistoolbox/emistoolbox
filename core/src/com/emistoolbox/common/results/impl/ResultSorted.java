package com.emistoolbox.common.results.impl;

import com.emistoolbox.common.results.Result;
import com.emistoolbox.common.results.ResultUtil;
import java.util.Arrays;
import java.util.Comparator;

public class ResultSorted implements Result
{
    private Result result;
    private int lookupDimension;
    private int[] lookup;

    public ResultSorted(Result result, int dimension) {
        this.result = result;
        this.lookupDimension = dimension;
        this.lookup = getLookupByName(result, dimension);
    }

    public ResultSorted(Result result, int dimension, boolean ascending) {
        this.result = result;
        this.lookupDimension = dimension;
        this.lookup = getLookup(result, dimension, ascending);
    }

    public double get(int[] indexes)
    {
        int oldIndex = indexes[this.lookupDimension];
        indexes[this.lookupDimension] = this.lookup[indexes[this.lookupDimension]];

        double tmp = this.result.get(indexes);
        indexes[this.lookupDimension] = oldIndex;

        return tmp;
    }

    public double get(String[] values)
    {
        int[] indexes = ResultUtil.getIndexes(this.result.getHeadings(), values);
        return get(indexes);
    }

    public int getDimensionSize(int i)
    {
        return this.result.getDimensionSize(i);
    }

    public int getDimensions()
    {
        return this.result.getDimensions();
    }

    public String getFormat(int indexLastDimension)
    {
        if (this.lookupDimension == this.result.getDimensions() - 1)
        {
            indexLastDimension = this.lookup[indexLastDimension];
        }
        return this.result.getFormat(indexLastDimension);
    }
    
    public String getHeading(int dimension, int index)
    {
        if (dimension == this.lookupDimension)
        {
            index = this.lookup[index];
        }
        return this.result.getHeading(dimension, index);
    }

    public String[] getHeadings(int dimension)
    {
        String[] headings = this.result.getHeadings(dimension);
        if (dimension != this.lookupDimension)
        {
            return headings;
        }
        return getSortedHeadings(headings);
    }

    public String[][] getHeadings()
    {
        String[][] tmp = this.result.getHeadings();
        tmp[this.lookupDimension] = getSortedHeadings(tmp[this.lookupDimension]);
        return tmp;
    }

    private String[] getSortedHeadings(String[] headings)
    {
        String[] result = new String[headings.length];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = headings[this.lookup[i]];
        }
        return result;
    }

    public String getValueLabel()
    {
        return this.result.getValueLabel();
    }

    public void set(int[] indexes, double value)
    {
        int oldIndex = indexes[this.lookupDimension];
        indexes[this.lookupDimension] = this.lookup[indexes[this.lookupDimension]];
        set(indexes, value);
        indexes[this.lookupDimension] = oldIndex;
    }

    public void set(String[] indexes, double value)
    {
        set(ResultUtil.getIndexes(this.result.getHeadings(), indexes), value);
    }

    public void setDefaultFormat(String defaultFormat)
    {
        this.result.setDefaultFormat(defaultFormat);
    }

    public void setFormat(int indexLastDimension, String format)
    {
        if (this.lookupDimension == getDimensions() - 1)
        {
            indexLastDimension = this.lookup[indexLastDimension];
        }
        this.result.setFormat(indexLastDimension, format);
    }

    public void setHeading(int dimension, int index, String val)
    {
        if (dimension == this.lookupDimension)
        {
            index = this.lookup[index];
        }
        this.result.setHeading(dimension, index, val);
    }

    public void setValueLabel(String label)
    {
        this.result.setValueLabel(label);
    }

    private static int[] getLookupByName(Result result, int dimension)
    {
        int dimensionSize = result.getDimensionSize(dimension);
        SortValue<String>[] lookup = new SortValue[dimensionSize];
        for (int i = 0; i < dimensionSize; i++)
        {
            lookup[i] = new SortValue<String>(i, result.getHeading(dimension, i));
        }
        Arrays.sort(lookup, new Comparator<SortValue<String>>() {
            public int compare(ResultSorted.SortValue<String> o1, ResultSorted.SortValue<String> o2)
            {
                return ((String) o1.value).compareTo((String) o2.value);
            }
        });
        return getLookupIndexes(lookup);
    }

    private static int[] getLookup(Result result, int dimension, final boolean ascending)
    {
        int[] indexes = new int[result.getDimensions()];
        int dimensionSize = result.getDimensionSize(dimension);

        SortValue<Double>[] lookup = new SortValue[dimensionSize];
        for (int i = 0; i < dimensionSize; i++)
        {
            indexes[dimension] = i;
            lookup[i] = new SortValue<Double>(i, Double.valueOf(result.get(indexes)));
        }

        Arrays.sort(lookup, new Comparator<SortValue<Double>>() {
            public int compare(ResultSorted.SortValue<Double> o1, ResultSorted.SortValue<Double> o2)
            {
                if (Double.isNaN(((Double) o1.value).doubleValue()))
                    o1.value = Double.valueOf(0.0D);
                if (Double.isNaN(((Double) o2.value).doubleValue()))
                {
                    o2.value = Double.valueOf(0.0D);
                }
                return (ascending ? 1 : -1) * Double.compare(((Double) o1.value).doubleValue(), ((Double) o2.value).doubleValue());
            }
        });
        return getLookupIndexes(lookup);
    }

    private static <T> int[] getLookupIndexes(SortValue<T>[] values)
    {
        int[] lookupIndexes = new int[values.length];
        for (int i = 0; i < values.length; i++)
        {
            lookupIndexes[i] = values[i].index;
        }
        return lookupIndexes;
    }

    static class SortValue<T>
    {
        int index;
        T value;

        public SortValue(int index, T value) {
            this.index = index;
            this.value = value;
        }
    }
}
