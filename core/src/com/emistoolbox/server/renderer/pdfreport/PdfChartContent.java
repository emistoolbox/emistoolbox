package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.ChartConfig.ChartType;
import com.emistoolbox.common.renderer.pdfreport.PdfChartContentConfig;
import com.emistoolbox.common.results.Result;

public abstract interface PdfChartContent extends PdfContentWithResult, PdfContent<PdfChartContentConfig>
{
	public ChartType getType();

	public ChartConfig getChartConfig();
	public void setChartConfig(ChartConfig config);
}
