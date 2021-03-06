package com.emistoolbox.common.results;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.ChartStroke;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;

public abstract interface ReportMetaResult extends MetaResult
{
    public abstract EmisPdfReportConfig getReportConfig();

    public abstract void setReportConfig(EmisPdfReportConfig paramPdfReportConfig);

    public abstract void setEntityPath(int[] paramArrayOfInt, String[] paramArrayOfString);

    public abstract int[] getEntityPathIds();

    public abstract String[] getEntityPathNames();
    
    public abstract ChartColor[] getColourScheme(); 
    public abstract void setColourScheme(ChartColor[] colors);
    
    public ChartStroke[] getStrokes(); 
    public void setStrokes(ChartStroke[] strokes); 
}
