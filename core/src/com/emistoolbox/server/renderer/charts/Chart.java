package com.emistoolbox.server.renderer.charts;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.Result;
import org.jfree.chart.JFreeChart;

public abstract interface Chart
{
    public abstract JFreeChart render(ChartConfig paramChartConfig, Result paramResult);
}
