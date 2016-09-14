package com.emistoolbox.server.renderer.charts.impl;

import com.emistoolbox.common.ChartStroke;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.ServerUtil;
import com.emistoolbox.server.renderer.pdfreport.fonts.FontUtils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.text.DecimalFormat;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.CategoryLabelWidthType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.Range;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

class AbstractBarChartRenderer extends AbstractChartRenderer
{
    public static void configureBarChart(ChartConfig config, JFreeChart chart, Result result)
    { configureChart(config, chart, result, 0); }
    
    public static void configureChart(ChartConfig config, JFreeChart chart, Result result, int seriesCount)
    {
        configureBase(config, chart);
        CategoryItemRenderer renderer = chart.getCategoryPlot().getRenderer();

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setDomainGridlinePaint(ChartUtil.getColor(config.getBackgroundColour()));
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(ChartUtil.getColor(config.getTextColour()));

        plot.setOutlineVisible(false);

        for (int i = 0; i < seriesCount; i++) 
        {
            BasicStroke wideLine = new BasicStroke( 3.0f );
            renderer.setSeriesStroke(i, wideLine);
        }
        
        CategoryAxis domainAxis = plot.getDomainAxis();

        checkAndRotateXAxisLabels(domainAxis, result.getDimensionSize(0));
        setAxisFontAndStroke(domainAxis);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        setAxisFontAndStroke(rangeAxis);
        rangeAxis.setNumberFormatOverride(new DecimalFormat("#,##0")); // config.getAxisFormat())); - just display whole numbers on axis.
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        plot.getDomainAxis().setMaximumCategoryLabelWidthRatio(1.8F);
        if (config.getYAxisLabel() != null)
        	plot.getRangeAxis().setLabel(config.getYAxisLabel());

        CategoryItemRenderer itemRenderer = plot.getRenderer();
        if (itemRenderer instanceof BarRenderer)
            ((BarRenderer) itemRenderer).setItemMargin(0.05D);
        else if (itemRenderer instanceof LineAndShapeRenderer)
            ((LineAndShapeRenderer) itemRenderer).setItemMargin(0.05D); 

        ((AbstractRenderer) itemRenderer).setBaseLegendTextFont(new Font(Font.SANS_SERIF, 0, 10));

        if (config.getShowValues())
        {
            renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat(ServerUtil.formatRemovePercent(config.getAxisFormat()))));
            renderer.setBaseItemLabelFont(FontUtils.getFont(config.getLabelFont()));
            renderer.setBaseItemLabelsVisible(true);
        }

        setColoursOfBars(config, result, renderer);

        Range dataRange = chart.getCategoryPlot().getDataRange(rangeAxis);
        double ubound = dataRange == null ? 1.0D : dataRange.getUpperBound();
        double maxThreshold = addThresholdMarkers(plot, config);

        if ((!Double.isNaN(maxThreshold)) && (ubound < maxThreshold))
        {
            ubound = maxThreshold + ubound / 100.0D * 10.0D;
        }

        if (!Double.isNaN(config.getMaxValue()))
        {
            ubound = config.getMaxValue();
        }
        rangeAxis.setUpperBound(ubound);
    }

    private static void setColoursOfBars(ChartConfig config, Result result, CategoryItemRenderer renderer)
    {
        boolean isSeries = false;

        int serieslen = result.getDimensions() > 1 ? 1 : 0;
        for (int i = 0; i < result.getDimensionSize(serieslen); i++)
        {
            Paint colour = ChartUtil.getColor(config.getFillColour());
            try
            {
                com.emistoolbox.common.ChartColor[] colSeries = config.getSeriesColours();
                colour = ChartUtil.getColor(result.getDimensions() == 1 ? colSeries[0] : colSeries[(i % colSeries.length)]);
                isSeries = true;
            }
            catch (NullPointerException np)
            {}
            catch (ArrayIndexOutOfBoundsException e)
            {}
            if (!isSeries)
            {
                boolean isThresholdColor = true;
                try
                { colour = ChartUtil.getColor(config.getNormalColour()); }
                catch (NullPointerException np)
                { isThresholdColor = false; }

                if (isThresholdColor)
                    colour = getThresholdColour(config, result.get(new int[] { i }));
            }

            if (renderer instanceof BarRenderer)
            {
                BarRenderer barRenderer = (BarRenderer) renderer;
                barRenderer.setShadowVisible(false);
                barRenderer.setBarPainter(new StandardBarPainter());

                renderer.setSeriesPaint(i, ChartUtil.getJFreePaint(colour));

                if (colour instanceof TexturePaint)
                {
                    barRenderer.setBaseOutlinePaint(ChartUtil.getJFreePaint(Color.BLACK));
                    barRenderer.setDrawBarOutline(true);
                }
            }
            
            if (renderer instanceof LineAndShapeRenderer)
            {
                LineAndShapeRenderer lineRenderer = (LineAndShapeRenderer) renderer; 
                lineRenderer.setSeriesPaint(i, ChartUtil.getJFreePaint(colour)); 
            	ChartStroke[] strokeSeries = config.getSeriesStrokes();
                if (isSeries && strokeSeries != null)
                {
                    ChartStroke stroke = result.getDimensions() == 1 ? strokeSeries[0] : strokeSeries[(i % strokeSeries.length)];
                    lineRenderer.setSeriesStroke(i, ChartUtil.getStroke(stroke));
                }
            }
        }
    }

    private static Paint getThresholdColour(ChartConfig config, double value)
    {
        Paint colour = null;
        double goodTHold = config.getGoodThreshold();
        double badTHold = config.getBadThreshold();

        if ((!Double.isNaN(goodTHold)) && (!Double.isNaN(badTHold)))
        {
            if (goodTHold > badTHold)
            {
                if (value > goodTHold)
                    colour = ChartUtil.getColor(config.getGoodColour());
                else if (value > badTHold)
                    colour = ChartUtil.getColor(config.getNormalColour());
                else
                {
                    colour = ChartUtil.getColor(config.getBadColour());
                }

            }
            else if (value > badTHold)
                colour = ChartUtil.getColor(config.getBadColour());
            else if (value > goodTHold)
                colour = ChartUtil.getColor(config.getNormalColour());
            else
                colour = ChartUtil.getColor(config.getGoodColour());
        }
        else if (!Double.isNaN(goodTHold))
        {
            if (value > goodTHold)
                colour = ChartUtil.getColor(config.isGoodThresholdHigherGood() ? config.getGoodColour() : config.getBadColour());
            else
                colour = ChartUtil.getColor(config.isGoodThresholdHigherGood() ? config.getBadColour() : config.getGoodColour());
        }
        else if (!Double.isNaN(badTHold))
        {
            throw new UnsupportedOperationException("Bad threshold only is not supported");
        }
        return colour;
    }

    private static double addThresholdMarkers(CategoryPlot plot, ChartConfig config)
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

        return goodThreshold > badThreshold ? goodThreshold : badThreshold;
    }

    private static void checkAndRotateXAxisLabels(CategoryAxis domainAxis, int dimensionSize)
    {
        if (dimensionSize > 6)
        {
            CategoryLabelPositions catpos = domainAxis.getCategoryLabelPositions();
            CategoryLabelPosition newpos = new CategoryLabelPosition(RectangleAnchor.TOP_RIGHT, TextBlockAnchor.BOTTOM_RIGHT, TextAnchor.BOTTOM_RIGHT, -1.570796326794897D, CategoryLabelWidthType.RANGE, 1.8F);
            catpos = CategoryLabelPositions.replaceBottomPosition(catpos, newpos);
            domainAxis.setCategoryLabelPositions(catpos);
        }
    }
}

