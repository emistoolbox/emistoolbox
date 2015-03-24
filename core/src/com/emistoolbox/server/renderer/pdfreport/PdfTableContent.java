package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.ChartFont;

public abstract interface PdfTableContent extends PdfResultContent
{
    public abstract void setFont(FontIdentifier paramFontIdentifier, ChartFont paramChartFont);
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.renderer.pdfreport.PdfTableContent
 * JD-Core Version: 0.6.0
 */