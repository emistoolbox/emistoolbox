package com.emistoolbox.server.renderer.charts.impl;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.renderer.charts.ChartRenderer;
import java.io.File;
import java.io.IOException;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

public class ChartRendererImpl implements ChartRenderer
{
    public void renderBar(Result result, ChartConfig config, File outputFile) throws IOException
    {
        JFreeChart chart = new BarChartRenderer().render(config, result);
        ChartUtilities.saveChartAsPNG(outputFile, chart, config.getChartWidth(), config.getChartHeight());
    }

    public void renderLines(Result result, ChartConfig config, File outputFile) 
        throws IOException
    {
        JFreeChart chart = new LineChartRenderer().render(config, result); 
        ChartUtilities.saveChartAsPNG(outputFile, chart, config.getChartWidth(), config.getChartHeight());
    }
    
    public void renderPie(Result result, ChartConfig config, File outputFile) throws IOException
    {
        JFreeChart chart = new PieChartRenderer().render(config, result);
        ChartUtilities.saveChartAsPNG(outputFile, chart, config.getChartWidth(), config.getChartHeight());
    }

    public void renderStackedBar(Result result, ChartConfig config, File outputFile) throws IOException
    {
        JFreeChart chart;
        if (result.getDimensions() == 1)
            chart = new BarChartRenderer().render(config, result);
        else
            chart = new StackedBarChartRenderer().render(config, result);

        ChartUtilities.saveChartAsPNG(outputFile, chart, config.getChartWidth(), config.getChartHeight());
    }

    public void renderNormalizedStackedBar(Result result, ChartConfig config, File outputFile) throws IOException
    {
        JFreeChart chart = new NormalizedStackedBarChartRenderer().render(config, result);
        ChartUtilities.saveChartAsPNG(outputFile, chart, config.getChartWidth(), config.getChartHeight());
    }

    public void renderTargetLineOverBarChart(Result result, ChartConfig config, double target, File outputFile) throws IOException
    {
        JFreeChart chart = new TargetLineOverBarChartRenderer().render(config, result, target);
        ChartUtilities.saveChartAsPNG(outputFile, chart, config.getChartWidth(), config.getChartHeight());
    }
}

