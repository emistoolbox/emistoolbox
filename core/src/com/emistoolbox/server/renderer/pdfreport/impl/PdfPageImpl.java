package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.ChartFont;
import com.emistoolbox.server.renderer.pdfreport.FontIdentifier;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
import com.emistoolbox.server.renderer.pdfreport.PdfContentFontMap;
import com.emistoolbox.server.renderer.pdfreport.PdfPage;

public class PdfPageImpl implements PdfPage
{
    private PdfContent[][] content;
    private String title;
    private String subtitle;
    private String footer;
    private int rows = 0;
    private int cols = 0;
    private PdfContentFontMap fontMap = new PdfContentFontMap();

    public void addContent(int row, int col, PdfContent content)
    {
        addContent(row, col, content, false);
    }

    private void addContent(int row, int col, PdfContent content, boolean clearing)
    {
        if ((row >= this.rows) || (col >= this.cols))
        {
            throw new IllegalArgumentException("Invalid Row or Column");
        }
        PdfContent currentContent = this.content[row][col];

        if (currentContent != null)
        {
            if (((currentContent instanceof PdfNullContent)) && (!clearing))
            {
                throw new IllegalStateException("Position currently occupied from a row/col span");
            }
            clearContent(row, col, currentContent);
            setContent(row, col, content);
        }
        else
        {
            setContent(row, col, content);
        }
    }

    private void clearContent(int row, int col, PdfContent currentContent)
    {
        if (currentContent.getSpanRows() > 1)
        {
            setSpanRows(row, col, currentContent.getSpanRows(), null, true);
        }
        if (currentContent.getSpanCols() > 1)
        {
            setSpanColumns(row, col, currentContent.getSpanRows(), null, true);
        }

        setContent(row, col, null);
    }

    private void setContent(int row, int col, PdfContent content)
    {
        if (content != null)
        {
            if (!checkSpanColumns(content.getSpanCols(), col, row))
            {
                throw new IllegalArgumentException("Cannot Span Columns");
            }
            if (!checkSpanRows(content.getSpanRows(), row, col))
            {
                throw new IllegalArgumentException("Cannot Span Rows");
            }

            setSpanColumns(row, col, content.getSpanCols(), PdfNullContent.getInstance(), false);
            setSpanRows(row, col, content.getSpanRows(), PdfNullContent.getInstance(), false);
        }

        this.content[row][col] = content;
    }

    private boolean checkSpanRows(int spanRows, int row, int col)
    {
        if (row + spanRows - 1 >= this.rows)
        {
            return false;
        }
        for (int i = 0; i < spanRows; i++)
        {
            if (this.content[(row + i)][col] != null)
                return false;
        }
        return true;
    }

    private boolean checkSpanColumns(int spanCols, int col, int row)
    {
        if (col + spanCols - 1 >= this.cols)
        {
            return false;
        }
        for (int i = 0; i < spanCols; i++)
        {
            if (this.content[row][(col + i)] != null)
                return false;
        }
        return true;
    }

    private void setSpanRows(int row, int col, int spanRows, PdfContent content, boolean clearing)
    {
        for (int i = 1; i < spanRows; i++)
            addContent(row + i, col, content, clearing);
    }

    private void setSpanColumns(int row, int col, int spanCols, PdfContent content, boolean clearing)
    {
        for (int i = 1; i < spanCols; i++)
            addContent(row, col + i, content, clearing);
    }

    public void setFooter(String footer)
    {
        this.footer = footer;
    }

    public void setLayout(int rows, int cols)
    {
        this.content = new PdfContent[rows][cols];
        this.rows = rows;
        this.cols = cols;
    }

    public void setTitle(String title, String subtitle)
    {
        this.title = title;
        this.subtitle = subtitle;
    }

    public int getColumns()
    {
        return this.cols;
    }

    public PdfContent getContent(int row, int col)
    {
        return this.content[row][col];
    }

    public int getRows()
    {
        return this.rows;
    }

    public String getSubtitle()
    {
        return this.subtitle;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getFooter()
    {
        return this.footer;
    }

    public void setFont(FontIdentifier identifier, ChartFont font)
    {
        this.fontMap.setFont(identifier, font);
    }

    public String contentToString()
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.rows; i++)
        {
            for (int j = 0; j < this.cols; j++)
            {
                PdfContent locContent = getContent(i, j);
                builder.append("|").append(locContent == null ? "Empty" : locContent.toString()).append("|");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public String toString()
    {
        return String.format("%s - %s : rows:%d  cols:%d", new Object[] { this.title, this.subtitle, Integer.valueOf(this.rows), Integer.valueOf(this.cols) });
    }
}
