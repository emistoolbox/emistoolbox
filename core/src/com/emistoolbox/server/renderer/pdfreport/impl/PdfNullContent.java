package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.server.renderer.pdfreport.PdfContent;

public class PdfNullContent implements PdfContent
{
    private static PdfNullContent instance = new PdfNullContent();

    public void setTitle(String title)
    {
        throw new UnsupportedOperationException();
    }

    public void setSpanCols(int cols)
    {
        throw new UnsupportedOperationException();
    }

    public void setSpanRows(int rows)
    {
        throw new UnsupportedOperationException();
    }

    public int getSpanCols()
    {
        return 0;
    }

    public int getSpanRows()
    {
        return 0;
    }

    public String toString()
    {
        return "PdfNullContent";
    }

    public String getTitle()
    {
        return "PdfNullContent";
    }

    public static PdfContent getInstance()
    {
        return instance;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.renderer.pdfreport.impl.PdfNullContent
 * JD-Core Version: 0.6.0
 */