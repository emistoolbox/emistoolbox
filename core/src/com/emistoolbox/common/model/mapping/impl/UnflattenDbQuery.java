package com.emistoolbox.common.model.mapping.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UnflattenDbQuery
{
    String name;
    String table;
    private List<UnflattenDbColumn> columns;
    private List<UnflattenDbRow> rows;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getTable()
    {
        return table;
    }

    public void setTable(String table)
    {
        this.table = table;
    }

    public void addColumn(UnflattenDbColumn col)
    {
        if (columns == null)
            columns = new ArrayList<UnflattenDbColumn>();
        columns.add(col);
    }

    public List<UnflattenDbColumn> getColumns()
    {
        return columns;
    }

    public void setColumns(List<UnflattenDbColumn> columns)
    {
        this.columns = columns;
    }

    public void addRow(UnflattenDbRow row)
    {
        if (rows == null)
            rows = new ArrayList<UnflattenDbRow>();
        rows.add(row);
    }

    public List<UnflattenDbRow> getRows()
    {
        return rows;
    }

    public void setRows(List<UnflattenDbRow> rows)
    {
        this.rows = rows;
    }

    public List<String> getFieldNames()
    {
        Set<String> result = new HashSet<String>();
        for (UnflattenDbColumn col : columns)
            result.add(col.getName());

        for (UnflattenDbRow row : rows)
            for (UnflattenDbColumn col : row.getColumns())
                result.add(col.getName());

        return new ArrayList<String>(result);
    }

    public Map<String, String> getAllValues(Map<String, String> dbRow, int rowIndex)
    {
        Map<String, String> result = new HashMap<String, String>();
        rows.get(rowIndex).setValues(result, dbRow);
        for (UnflattenDbColumn col : columns)
            col.setValue(result, dbRow);

        return result;
    }
}
