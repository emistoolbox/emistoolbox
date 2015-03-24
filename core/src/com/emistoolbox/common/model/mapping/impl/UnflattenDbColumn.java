package com.emistoolbox.common.model.mapping.impl;

import java.util.Map;

import com.emistoolbox.client.admin.ui.EmisUtils;

public class UnflattenDbColumn
{
    private String name;
    private String column;
    private String value;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setColumn(String column)
    {
        this.column = column;
    }
    
    public String getColumn()
    { return column; }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getValue(Map<String, String> dbRow)
    {

        if (EmisUtils.isEmpty(column))
            return value;

        return dbRow.get(column);
    }

    public void setValue(Map<String, String> targetDbRow, Map<String, String> dbRow)
    {
        targetDbRow.put(getName(), getValue(dbRow));
    }

}
