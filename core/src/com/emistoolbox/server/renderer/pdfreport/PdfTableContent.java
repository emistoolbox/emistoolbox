package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.ChartFont;

public interface PdfTableContent extends PdfContent
{
    public void setFont(FontIdentifier paramFontIdentifier, ChartFont paramChartFont);
    public ChartFont getFont(FontIdentifier paramFontIdentifier);
}
