package com.emistoolbox.server.renderer.charts.impl;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.renderer.charts.Chart;
import java.awt.BasicStroke;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class TargetLineOverBarChartRenderer extends AbstractBarChartRenderer implements Chart
{
    public JFreeChart render(ChartConfig config, Result result, double target)
    {
        JFreeChart chart = null;

        if (result.getDimensions() == 1)
            chart = getChart1D(result, config, target);
        else
        {
            throw new UnsupportedOperationException("Currently only supports single dimension");
        }
        return chart;
    }

    private JFreeChart getChart1D(Result result, ChartConfig config, double uBound)
    {
        JFreeChart chart = ChartFactory.createStackedBarChart("", "", "", ResultToDatasetUtils.get1DCategoryDataset(result), PlotOrientation.VERTICAL, false, false, false);
        configureBarChart(config, chart, result);

        CategoryPlot plot = chart.getCategoryPlot();

        CategoryDataset mainDataset = plot.getDataset();
        int datasetSize = mainDataset.getColumnCount();

        DefaultCategoryDataset lineDataset = new DefaultCategoryDataset();
        double lBound = mainDataset.getValue(0, 0).doubleValue();

        lineDataset.addValue(lBound, "linePlot", mainDataset.getColumnKey(0));

        for (int i = 1; i < datasetSize; i++)
        {
            lineDataset.addValue(lBound + (uBound - lBound) / (datasetSize - 1) * i, "linePlot", mainDataset.getColumnKey(i));
        }
        lineDataset.addValue(uBound, "linePlot", mainDataset.getColumnKey(datasetSize - 1));

        CategoryItemRenderer lineRenderer = new LineAndShapeRenderer();
        lineRenderer.setSeriesStroke(0, new BasicStroke(2.0F));
        plot.setDataset(1, lineDataset);
        plot.setRenderer(1, lineRenderer);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        return chart;
    }

    public JFreeChart render(ChartConfig config, Result result)
    {
        throw new UnsupportedOperationException("Must have a target for the overlay");
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.server.renderer.charts.impl.TargetLineOverBarChartRenderer
 * JD-Core Version: 0.6.0
 */