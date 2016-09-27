package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.ChartConfig.ChartType;
import com.emistoolbox.common.renderer.pdfreport.PdfChartContentConfig;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfChartContentConfigImpl;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.renderer.pdfreport.PdfChartContent;

public class PdfChartContentImpl extends PdfContentBase<PdfChartContentConfig> implements PdfChartContent
{
    private ChartConfig config;
    private Result result;
    private final ChartType type;

    public PdfChartContentImpl(int chartType) 
    { this(ChartType.values()[chartType]); }

    public PdfChartContentImpl(ChartType type) 
    { this.type = type; }

    @Override
	public void setChartConfig(ChartConfig config)
    { this.config = config; }

    @Override
	public void setResult(Result result)
    { this.result = result; }

    @Override
	public ChartConfig getChartConfig() 
    { return config; }

	@Override
	public Result getResult() 
	{ return result; }

	@Override
	public ChartType getType() 
	{ return type; }

	public String toString()
    { return String.format("%s - %s", new Object[] { getClass().getSimpleName(), this.type }); }
}
