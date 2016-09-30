package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.TableStyleConfig;

public interface PdfTableContent extends TableStyleConfig
{
    public void setFont(FontIdentifier paramFontIdentifier, ChartFont paramChartFont);
    public ChartFont getFont(FontIdentifier paramFontIdentifier);
        
    public int getColumns(); 
    public int getRows(); 
    
    public String getText(int row, int col);    
}
