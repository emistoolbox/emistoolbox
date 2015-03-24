package com.emistoolbox.server.renderer.charts.impl;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.ServerUtil;
import com.emistoolbox.server.renderer.charts.Chart;
import java.text.DecimalFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;

public class XYScatterChartRenderer extends AbstractChartRenderer implements Chart
{
    public JFreeChart render(ChartConfig config, Result result)
    {
        XYDataset dataset;
        if (result.getDimensions() == 2)
        {
            dataset = ResultToDatasetUtils.get2DXYDataset(result);
        }
        else
        {
            if (result.getDimensions() > 2)
                dataset = ResultToDatasetUtils.getMultiSeriesXYDataset(result);
            else
                throw new UnsupportedOperationException();
        }
        JFreeChart chart = ChartFactory.createScatterPlot("", result.getHeading(0, 0), result.getHeading(0, 1), dataset, PlotOrientation.VERTICAL, true, false, false);

        configureXYPlotChart(config, chart, result);

        return chart;
    }

    private static void configureXYPlotChart(ChartConfig config, JFreeChart chart, Result result)
    {
        configureBase(config, chart);

        XYPlot plot = (XYPlot) chart.getPlot();
        XYItemRenderer renderer = plot.getRenderer();

        plot.setDomainGridlinePaint(ChartUtil.getColor(config.getBackgroundColour()));
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(ChartUtil.getColor(config.getTextColour()));

        Axis domainAxis = plot.getDomainAxis();

        setAxisFontAndStroke(domainAxis);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        setAxisFontAndStroke(rangeAxis);
        rangeAxis.setNumberFormatOverride(new DecimalFormat(ServerUtil.formatRemovePercent(config.getAxisFormat())));

        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        if (config.getShowValues())
        {
            renderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator("{2}", new DecimalFormat(ServerUtil.formatRemovePercent(config.getAxisFormat())), new DecimalFormat(ServerUtil.formatRemovePercent(config.getAxisFormat()))));

            renderer.setBaseItemLabelFont(CHART_FONT);
            renderer.setBaseItemLabelsVisible(true);
        }

        addThresholdMarkers(plot, config);
    }

    private static void addThresholdMarkers(XYPlot plot, ChartConfig config)
    {
        double goodThreshold = config.getGoodThreshold();
        double badThreshold = config.getBadThreshold();

        boolean labelAbove = true;

        if ((!Double.isNaN(goodThreshold)) && (!Double.isNaN(badThreshold)))
            labelAbove = goodThreshold > badThreshold;
        else if (!Double.isNaN(goodThreshold))
        {
            labelAbove = config.isGoodThresholdHigherGood();
        }
        plot.addRangeMarker(getMarker(ChartUtil.getColor(config.getTextColour()), config.getGoodThresholdText(), goodThreshold, labelAbove));
        plot.addRangeMarker(getMarker(ChartUtil.getColor(config.getTextColour()), config.getBadThresholdText(), badThreshold, !labelAbove));
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.server.renderer.charts.impl.XYScatterChartRenderer JD-Core
 * Version: 0.6.0
 */