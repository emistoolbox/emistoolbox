package com.emistoolbox.server.renderer.charts.impl;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.renderer.charts.Chart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

public class BarChartRenderer extends AbstractBarChartRenderer implements Chart
{
    public JFreeChart render(ChartConfig config, Result result)
    {
        JFreeChart chart = null;

        if (result.getDimensions() == 1)
            chart = getChart1D(result, config);
        else if (result.getDimensions() == 2)
            chart = getChart2D(result, config);
        else
        {
            throw new UnsupportedOperationException();
        }
        return chart;
    }

    private JFreeChart getChart1D(Result result, ChartConfig config)
    {
        JFreeChart chart = ChartFactory.createStackedBarChart("", "", "", ResultToDatasetUtils.get1DCategoryDataset(result), PlotOrientation.VERTICAL, false, false, false);
        configureBarChart(config, chart, result);
        return chart;
    }

    private JFreeChart getChart2D(Result result, ChartConfig config)
    {
        JFreeChart chart = ChartFactory.createBarChart("", "", "", ResultToDatasetUtils.get2DCategoryDataset(result), PlotOrientation.VERTICAL, true, false, false);
        configureBarChart(config, chart, result);
        return chart;
    }
}
