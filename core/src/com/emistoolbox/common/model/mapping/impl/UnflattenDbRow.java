package com.emistoolbox.common.model.mapping.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnflattenDbRow
{
    public List<UnflattenDbColumn> columns;

    public List<UnflattenDbColumn> getColumns()
    { return columns; }

    public void setColumns(List<UnflattenDbColumn> columns)
    { this.columns = columns; }

    public void addColumn(UnflattenDbColumn col)
    {
        if (columns == null)
            columns = new ArrayList<UnflattenDbColumn>();
        columns.add(col);
    }

    public void setValues(Map<String, String> targetDbRow, Map<String, String> dbRow)
    {
        if (columns == null)
            return;

        for (UnflattenDbColumn column : columns)
            column.setValue(targetDbRow, dbRow);
    }
}
