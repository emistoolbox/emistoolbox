package com.emistoolbox.server.renderer.charts.impl;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.renderer.charts.Chart;
import com.emistoolbox.server.renderer.pdfreport.fonts.FontUtils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;

public class StackedBarChartRenderer extends AbstractBarChartRenderer implements Chart
{
    public JFreeChart render(ChartConfig config, Result result)
    {
        JFreeChart chart = null;

        if (result.getDimensions() == 2)
            chart = getChart2DStacked(result, config);
        else
            throw new UnsupportedOperationException();

        return chart;
    }

    private JFreeChart getChart2DStacked(Result result, ChartConfig config)
    {
        JFreeChart chart = ChartFactory.createStackedBarChart("", "", "", ResultToDatasetUtils.get2DCategoryDataset(result), PlotOrientation.VERTICAL, true, false, false);
        configure(config, chart, result);
        return chart;
    }

    private static void configure(ChartConfig config, JFreeChart chart, Result result)
    {
        if (config.getShowTotals())
        {
            int seriesCount = result.getDimensionSize(1); // result is always 2D, series is second dimension.
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setRenderer(new ExtendedStackedBarRenderer(seriesCount, FontUtils.getFont(config.getLabelFont())));
        }
        
        configureBarChart(config, chart, result);
    }
}
