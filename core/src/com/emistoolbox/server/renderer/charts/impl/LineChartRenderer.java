package com.emistoolbox.server.renderer.charts.impl;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.Result;

public class LineChartRenderer extends AbstractChartRenderer
{
    public JFreeChart render(ChartConfig config, Result result)
    {
        JFreeChart chart = null;
        if (result.getDimensions() == 1)
            chart = getChart1D(result, config);
        else if (result.getDimensions() == 2)
            chart = getChart2D(result, config);
        else
            throw new UnsupportedOperationException();

        return chart;
    }

    private JFreeChart getChart1D(Result result, ChartConfig config)
    {
        JFreeChart chart = ChartFactory.createLineChart("", "", "", ResultToDatasetUtils.get1DCategoryDatasetAsSeries(result), PlotOrientation.VERTICAL, false, false, false);
        AbstractBarChartRenderer.configureChart(config, chart, result, result.getDimensionSize(0));

        return chart;
    }

    private JFreeChart getChart2D(Result result, ChartConfig config)
    {
        JFreeChart chart = ChartFactory.createLineChart("", "", "", ResultToDatasetUtils.get2DCategoryDataset(result), PlotOrientation.VERTICAL, true, false, false);
        AbstractBarChartRenderer.configureChart(config, chart, result, result.getDimensionSize(1));

        return chart;
    }
}
