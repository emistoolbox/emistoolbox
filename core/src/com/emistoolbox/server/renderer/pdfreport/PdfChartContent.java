package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.ChartConfig.ChartType;
import com.emistoolbox.common.results.Result;

public abstract interface PdfChartContent extends PdfResultContent
{
	public ChartType getType();

	public Result getResult();
	public void setResult(Result result);

	public ChartConfig getChartConfig();
	public void setChartConfig(ChartConfig config);
}
