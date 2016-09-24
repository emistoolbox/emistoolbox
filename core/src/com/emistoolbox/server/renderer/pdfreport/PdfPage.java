package com.emistoolbox.server.renderer.pdfreport;

import java.util.List;

import com.emistoolbox.common.ChartFont;

import info.joriki.graphics.Point;
import info.joriki.graphics.Rectangle;

public interface PdfPage extends EmisPdfPage
{
    public void setLayout(int paramInt1, int paramInt2);
    public void addContent(int paramInt1, int paramInt2, PdfContent paramPdfContent);

    public List<PdfContent> getContents();
    public PdfContent getContent(int paramInt1, int paramInt2);

    public int getRows();
    public int getColumns();

    public void layout(Point pageSize, Rectangle margins, Point cellMargin); 
}
