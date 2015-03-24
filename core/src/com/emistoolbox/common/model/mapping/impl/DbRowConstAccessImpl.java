package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbRowConstAccess;

import java.io.Serializable;
import java.util.Map;

public class DbRowConstAccessImpl extends DbRowAccessBase implements DbRowConstAccess, Serializable
{
    private static final long serialVersionUID = 1L;
    private String value;

    public String getConstValue()
    {
        return this.value;
    }

    public void setConstValue(String value)
    {
        this.value = value;
    }

    public String getValue(int valueIndex, Map<String, String> row)
    {
        return this.value;
    }

    public int getValuesPerRow()
    {
        return 1;
    }

	@Override
	public String getInfo() 
	{ return "'" + value + "'"; }
}
