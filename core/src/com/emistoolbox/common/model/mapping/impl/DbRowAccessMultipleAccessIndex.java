package com.emistoolbox.common.model.mapping.impl;

import java.io.Serializable;
import java.util.Map;

public class DbRowAccessMultipleAccessIndex extends DbRowAccessBase implements Serializable
{
    private static final long serialVersionUID = 1L;
    private byte size;

    public void setSize(byte size)
    {
        this.size = size;
    }

    public int getSize()
    {
        return this.size;
    }

    public String getValue(int valueIndex, Map<String, String> row)
    {
        return "" + getValueAsInt(valueIndex, -1, row);
    }

    public int getValueAsInt(int valueIndex, int defaultValue, Map<String, String> row)
    {
        return valueIndex;
    }

    public int getValuesPerRow()
    {
        return this.size;
    }
    
    public String getInfo()
    { return "MultipleAccessIndex"; }
}
