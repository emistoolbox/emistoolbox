package com.emistoolbox.server.renderer.charts.impl;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.ServerUtil;
import com.emistoolbox.server.renderer.charts.Chart;
import com.emistoolbox.server.renderer.pdfreport.fonts.FontUtils;

import java.text.DecimalFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.TableOrder;

public class PieChartRenderer extends AbstractChartRenderer implements Chart
{
    private static final double DEFAULT_PERCENTAGE_LIMIT = 5.0D;

    public JFreeChart render(ChartConfig config, Result result)
    {
        JFreeChart chart = null;

        if (result.getDimensions() == 1)
            chart = getPieChart(result, config);
        else if (result.getDimensions() == 2)
            chart = getPieChartMulti(result, config);
        else
        {
            throw new UnsupportedOperationException();
        }
        return chart;
    }

    private JFreeChart getPieChart(Result result, ChartConfig config)
    {
        JFreeChart chart = ChartFactory.createPieChart("", ResultToDatasetUtils.get1DPieDataset(result), true, false, false);
        configure(config, chart, result);
        return chart;
    }

    private JFreeChart getPieChartMulti(Result result, ChartConfig config)
    {
        JFreeChart chart = ChartFactory.createMultiplePieChart("", ResultToDatasetUtils.get2DCategoryDataset(result), TableOrder.BY_COLUMN, true, false, false);
        configureMulti(config, chart, result);
        return chart;
    }

    private void configureMulti(ChartConfig config, JFreeChart chart, Result result)
    {
        configureBase(config, chart);
        MultiplePiePlot plot = (MultiplePiePlot) chart.getPlot();

        plot.setOutlineVisible(false);
        plot.getPieChart().getPlot().setOutlineVisible(false);

        setPiePlotSectionColors(config, (PiePlot) plot.getPieChart().getPlot(), result.getHeadings(1));
        setPiePlotLabels(config, (PiePlot) plot.getPieChart().getPlot(), "0%");
    }

    private void configure(ChartConfig config, JFreeChart chart, Result result)
    {
        configureBase(config, chart);
        PiePlot plot = (PiePlot) chart.getPlot();

        plot.setOutlineVisible(false);
        setPiePlotSectionColors(config, plot, result.getHeadings(0));
        setPiePlotLabels(config, plot, "0.0%");
    }

    private static void setPiePlotSectionColors(ChartConfig config, PiePlot plot, String[] keys)
    {
        if (config.getSeriesColours() != null)
        {
            ChartColor[] colSeries = config.getSeriesColours();
            int loop = 0;
            for (String key : keys)
            {
                plot.setSectionPaint(key, ChartUtil.getColor(colSeries[(loop % colSeries.length)]));
                loop++;
            }
        }
    }

    private static void setPiePlotLabels(ChartConfig config, PiePlot piePlot, String percentageFormat)
    {
        piePlot.setLabelLinksVisible(false);
        piePlot.setLabelBackgroundPaint(null);
        piePlot.setLabelOutlinePaint(null);
        piePlot.setLabelShadowPaint(null);
        piePlot.setLabelFont(FontUtils.getFont(config.getLabelFont())); 
        piePlot.setBackgroundPaint(null);

        piePlot.setLabelGap(0.06D);
        piePlot.setLabelPadding(new RectangleInsets(7.0D, -15.0D, 2.0D, -15.0D));

        if (config.getShowTotals())
        {
            piePlot.setLabelGenerator(new PercentLimitedPieSectionLabelGenerator("{1}({2})", new DecimalFormat(ServerUtil.formatRemovePercent(config.getAxisFormat())), new DecimalFormat(percentageFormat), 5.0D));
        }
    }
}
