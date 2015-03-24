package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.renderer.ChartConfig;

public abstract interface PdfChartContent extends PdfResultContent
{
    public abstract void setChartConfig(ChartConfig paramChartConfig);
}
