package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbRowFieldAccess;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class DbRowFieldAccessImpl extends DbRowAccessBase implements DbRowFieldAccess, Serializable
{
    private static final long serialVersionUID = 1L;
    private String field;

    public String getFieldName()
    {
        return this.field;
    }

    public void setFieldName(String name)
    {
        this.field = name;
    }

    public String getValue(int valueIndex, Map<String, String> row)
    {
        return (String) row.get(this.field);
    }

    public int getValuesPerRow()
    {
        return 1;
    }

    public void addColumns(Set<String> columns)
    {
        columns.add(this.field);
    }
    
    public String getInfo()
    { return "field:" + field; }
}
