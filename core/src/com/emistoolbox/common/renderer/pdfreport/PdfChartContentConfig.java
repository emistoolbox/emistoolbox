package com.emistoolbox.common.renderer.pdfreport;

import com.emistoolbox.common.results.TableMetaResult;

public interface PdfChartContentConfig extends PdfMetaResultContentConfig<TableMetaResult> 
{
    public int getChartType(); 
    public void setChartType(int chartType); 
}
