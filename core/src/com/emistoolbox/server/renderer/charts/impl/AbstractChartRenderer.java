package com.emistoolbox.server.renderer.charts.impl;

import com.emistoolbox.common.renderer.ChartConfig;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Paint;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

abstract class AbstractChartRenderer
{
    protected static final Font CHART_FONT = new Font("SansSerif", 1, 11);
    protected static final BasicStroke CHART_STROKE = new BasicStroke(2.0F);

    protected static void configureBase(ChartConfig config, JFreeChart chart)
    {
        chart.setBackgroundPaint(ChartUtil.getColor(config.getBackgroundColour()));

        Plot plot = chart.getPlot();
        plot.setNoDataMessage(config.getNoDataText());
        plot.setBackgroundPaint(ChartUtil.getColor(config.getBackgroundColour()));

        LegendTitle legend = chart.getLegend();
        if (legend != null)
            legend.setPosition(RectangleEdge.RIGHT);
    }

    protected static Marker getMarker(Paint colour, String text, double value, boolean labelAbove)
    {
        Marker marker = new ValueMarker(value);
        marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        marker.setPaint(colour);
        marker.setStroke(CHART_STROKE);
        marker.setLabelFont(CHART_FONT);
        marker.setLabel(text);
        marker.setLabelPaint(colour);
        marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        marker.setLabelTextAnchor(labelAbove ? TextAnchor.BOTTOM_LEFT : TextAnchor.TOP_LEFT);
        return marker;
    }

    protected static void setAxisFontAndStroke(Axis axis)
    {
        axis.setTickLabelFont(CHART_FONT);
        axis.setAxisLineStroke(CHART_STROKE);
    }
}
