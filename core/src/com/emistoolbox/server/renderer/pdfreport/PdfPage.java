package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.ChartFont;

public abstract interface PdfPage
{
    public abstract void setLayout(int paramInt1, int paramInt2);

    public abstract void addContent(int paramInt1, int paramInt2, PdfContent paramPdfContent);

    public abstract void setTitle(String paramString1, String paramString2);

    public abstract void setFooter(String paramString);

    public abstract void setFont(FontIdentifier paramFontIdentifier, ChartFont paramChartFont);

    public abstract PdfContent getContent(int paramInt1, int paramInt2);

    public abstract int getRows();

    public abstract int getColumns();

    public abstract String getTitle();

    public abstract String getSubtitle();

    public abstract String getFooter();
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.renderer.pdfreport.PdfPage JD-Core
 * Version: 0.6.0
 */