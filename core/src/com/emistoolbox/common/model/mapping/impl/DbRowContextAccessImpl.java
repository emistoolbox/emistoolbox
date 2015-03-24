package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbRowContextAccess;

import java.io.Serializable;
import java.util.Map;

public class DbRowContextAccessImpl extends DbRowAccessBase implements DbRowContextAccess, Serializable
{
    private static final long serialVersionUID = 1L;
    private String contextParam;
    private String paramInternal;

    public String getContextParameter()
    {
        return this.contextParam;
    }

    public void setContextParameter(String param)
    {
        this.contextParam = param;
        this.paramInternal = ("[" + param + "]");
    }

    public String getValue(int valueIndex, Map<String, String> row)
    {
        return (String) row.get(this.paramInternal);
    }

    public int getValuesPerRow()
    { return 1; }

	@Override
	public String getInfo() 
	{ return "context:" + contextParam; }
}
