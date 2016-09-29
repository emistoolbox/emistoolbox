package com.emistoolbox.common.renderer.pdfreport.impl;

import com.emistoolbox.common.renderer.pdfreport.PdfChartContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfigVisitor;
import com.emistoolbox.common.results.TableMetaResult;
import java.io.Serializable;

public class PdfChartContentConfigImpl extends PdfMetaResultContentConfigImpl<TableMetaResult> implements PdfChartContentConfig, Serializable
{
    private int chartType;

    public int getChartType()
    { return this.chartType; }

    public void setChartType(int chartType)
    { this.chartType = chartType; }
    
	@Override
	public <T> T accept(PdfContentConfigVisitor<T> visitor) 
	{ return visitor.visit(this); }
}
