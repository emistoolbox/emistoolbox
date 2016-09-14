package com.emistoolbox.server.renderer.pdfreport;

public abstract interface PdfContent
{
    public abstract void setTitle(String paramString);

    public abstract void setSpanRows(int paramInt);

    public abstract void setSpanCols(int paramInt);

    public abstract int getSpanRows();

    public abstract int getSpanCols();

    public abstract String getTitle();
}
