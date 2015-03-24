package com.emistoolbox.server.renderer.charts.impl;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.renderer.charts.Chart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.Range;

public class NormalizedStackedBarChartRenderer extends AbstractBarChartRenderer implements Chart
{
    public JFreeChart render(ChartConfig config, Result result)
    {
        JFreeChart chart = null;

        if (result.getDimensions() == 2)
            chart = getChart2DStacked(result, config);
        else
        {
            throw new UnsupportedOperationException();
        }
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
        CategoryPlot plot = chart.getCategoryPlot();
        StackedBarRenderer renderer;
        if (config.getMaxValue() > 0.0D)
        {
            plot.getRangeAxis().setRange(new Range(0.0D, config.getMaxValue()));
            renderer = new NormalizedStackedBarRenderer(config.getMaxValue());
        }
        else
        {
            renderer = new NormalizedStackedBarRenderer();
        }
        plot.setRenderer(renderer);
        configureBarChart(config, chart, result);
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.server.renderer.charts.impl.NormalizedStackedBarChartRenderer
 * JD-Core Version: 0.6.0
 */