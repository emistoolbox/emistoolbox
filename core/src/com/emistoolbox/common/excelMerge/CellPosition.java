package com.emistoolbox.common.excelMerge;

import java.io.Serializable;

public class CellPosition implements Serializable
{
    private int column;
    private int row;

    public CellPosition()
    {}
    
    public CellPosition(String name)
    {
        int pos = getFirstDigit(name);
        if (pos == -1)
            throw new IllegalArgumentException("Invalid Excel cell name: " + name);

        this.column = getColumnIndex(name.substring(0, pos));
        this.row = Integer.parseInt(name.substring(pos));
    }

    private int getFirstDigit(String name)
    {
        for (int i = 0; i < name.length(); i++)
            if (Character.isDigit(name.charAt(i)))
                return i;

        return -1;
    }

    public CellPosition(String columnName, int row)
    {
        this.column = getColumnIndex(columnName);
        this.row = row;
    }

    public CellPosition(CellPosition anchor, int rowOffset, int colOffset)
    {
        this.column = anchor.column + colOffset;
        this.row = anchor.row + rowOffset;
    }

    public int getRowIndex()
    { return row - 1; }

    public int getColumnIndex()
    { return column; }

    public String getColumn()
    { return getColumnName(column); }

    public int getRow()
    { return row; }

    public String toString()
    { return getColumnName(column) + row; }

    private String getColumnName(int index)
    {
        StringBuffer result = new StringBuffer();
        int charOffset = 0;
        do
        {
            result.insert(0, (char) ('A' + index % 26 + charOffset));
            index = index / 26;
            charOffset = 1;
        }
        while (index > 0);

        return result.toString();
    }

    private int getColumnIndex(String name)
    {
        int result = 0;
        name = name.toUpperCase();
        for (int i = 0; i < name.length(); i++)
        {
            result *= 26;
            result += name.charAt(i) - 'A' + 1;
        }

        return result - 1;
    }
}
