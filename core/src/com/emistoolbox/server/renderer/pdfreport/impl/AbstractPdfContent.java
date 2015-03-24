package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.server.renderer.pdfreport.PdfContent;

public abstract class AbstractPdfContent implements PdfContent
{
    private int colSpan = 1;
    private int rowSpan = 1;
    private String title;

    public int getSpanCols()
    {
        return this.colSpan;
    }

    public int getSpanRows()
    {
        return this.rowSpan;
    }

    public void setSpanCols(int cols)
    {
        this.colSpan = cols;
    }

    public void setSpanRows(int rows)
    {
        this.rowSpan = rows;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String toString()
    {
        return String.format("%s (cols:%d,rows:$d)", new Object[] { this.title, Integer.valueOf(this.colSpan), Integer.valueOf(this.rowSpan) });
    }

    public String getTitle()
    {
        return this.title;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.server.renderer.pdfreport.impl.AbstractPdfContent JD-Core
 * Version: 0.6.0
 */