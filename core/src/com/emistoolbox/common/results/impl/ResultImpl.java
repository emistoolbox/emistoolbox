package com.emistoolbox.common.results.impl;

import com.emistoolbox.common.FlatArrayUtil;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.common.results.ResultUtil;
import java.io.Serializable;

public class ResultImpl implements Result, Serializable
{
    private static final long serialVersionUID = 1L;
    int _dims = 0;
    int[] _sizes = null;
    double[] _data = null;
    String[][] _headers = (String[][]) null;
    String valueLabel = null;
    String defaultFormat = "#,##0.00";
    String[] formats = null;

    public ResultImpl() {
    }

    public ResultImpl(int[] dimensions) {
        this._dims = dimensions.length;
        this._sizes = new int[dimensions.length];
        for (int i = 0; i < dimensions.length; i++)
        {
            this._sizes[i] = dimensions[i];
        }
        this._headers = new String[this._dims][];

        int datasize = 1;
        for (int i = 0; i < this._dims; i++)
        {
            this._headers[i] = new String[this._sizes[i]];
            datasize *= this._sizes[i];
        }

        this._data = new double[datasize];
        for (int i = 0; i < datasize; i++)
        {
            this._data[i] = (0.0D / 0.0D);
        }
        this.formats = new String[dimensions[(dimensions.length - 1)]];
    }

    public int getDimensions()
    {
        return this._dims;
    }

    public int getDimensionSize(int i)
    {
        return this._sizes[i];
    }

    public String getHeading(int dimension, int index)
    {
        return this._headers[dimension][index];
    }

    public String[] getHeadings(int dimension)
    {
        return this._headers[dimension];
    }

    public String[][] getHeadings()
    {
        return this._headers;
    }

    public void setHeading(int dimension, int index, String val)
    {
        this._headers[dimension][index] = val;
    }

    public double get(int[] indexes)
    {
        int index = FlatArrayUtil.getFlatIndex(indexes, this._sizes);
        if (index == -1)
            return -1.0D;
        return this._data[index];
    }

    public double get(String[] indexes)
    {
        return get(ResultUtil.getIndexes(this._headers, indexes));
    }

    public void set(int[] indexes, double value)
    {
        int index = FlatArrayUtil.getFlatIndex(indexes, this._sizes);
        if (index == -1)
        {
            return;
        }
        this._data[index] = value;
    }

    public void set(String[] indexes, double value)
    {
        set(ResultUtil.getIndexes(this._headers, indexes), value);
    }

    public void merge(Result[] results)
    {
        ResultImpl[] source = new ResultImpl[results.length];
        for (int i = 0; i < results.length; i++)
        {
            if (!(results[0] instanceof ResultImpl))
            {
                throw new IllegalArgumentException("Cannot merge none ResultImpl objects.");
            }
            source[i] = ((ResultImpl) results[i]);
            if (source[i]._data.length * results.length != this._data.length)
            {
                throw new IllegalArgumentException("Cannot merge Results of different sizes.");
            }
        }

        for (int i = 0; i < results[0].getDimensions(); i++)
        {
            this._headers[i] = source[0]._headers[i];
        }
        int index = 0;
        for (int i = 0; i < source[0]._data.length; i++)
            for (int j = 0; j < source.length; j++)
            {
                this._data[index] = source[j]._data[i];
                index++;
            }
    }

    public String getValueLabel()
    {
        return this.valueLabel;
    }

    public void setValueLabel(String label)
    {
        this.valueLabel = label;
    }

    public String getFormat(int indexLastDimension)
    {
        if (this.formats[indexLastDimension] == null)
            return this.defaultFormat;
        return this.formats[indexLastDimension];
    }

    public void setFormat(int indexLastDimension, String format)
    {
        this.formats[indexLastDimension] = format;
    }

    public void setDefaultFormat(String defaultFormat)
    {
        this.defaultFormat = defaultFormat;
    }
}
