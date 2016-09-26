package com.emistoolbox.server.renderer.charts.impl;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.renderer.charts.ChartRenderer;

import es.jbauer.lib.io.IOFileInfo;
import es.jbauer.lib.io.IOOutput;
import es.jbauer.lib.io.impl.IOFileOutput;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

public class ChartRendererImpl extends BaseChartRenderer implements ChartRenderer
{
	
    @Override
	public boolean canCreateContentType(String contentType) 
    { return contentType.equals("image/png"); }

	public void renderBar(Result result, ChartConfig config, IOOutput outputFile) throws IOException
    {
        JFreeChart chart = new BarChartRenderer().render(config, result);
        
        File out = getFile(outputFile); 
        ChartUtilities.saveChartAsPNG(out, chart, config.getChartWidth(), config.getChartHeight());
        update(out, outputFile); 
    }

    public void renderLines(Result result, ChartConfig config, IOOutput outputFile) 
        throws IOException
    {
        JFreeChart chart = new LineChartRenderer().render(config, result); 
        
        File out = getFile(outputFile); 
        ChartUtilities.saveChartAsPNG(out, chart, config.getChartWidth(), config.getChartHeight());
        update(out, outputFile); 
    }
    
    public void renderPie(Result result, ChartConfig config, IOOutput outputFile) throws IOException
    {
        JFreeChart chart = new PieChartRenderer().render(config, result);
        
        File out = getFile(outputFile); 
        ChartUtilities.saveChartAsPNG(out, chart, config.getChartWidth(), config.getChartHeight());
        update(out, outputFile); 
    }

    public void renderStackedBar(Result result, ChartConfig config, IOOutput outputFile) throws IOException
    {
        JFreeChart chart;
        if (result.getDimensions() == 1)
            chart = new BarChartRenderer().render(config, result);
        else
            chart = new StackedBarChartRenderer().render(config, result);

        File out = getFile(outputFile); 
        ChartUtilities.saveChartAsPNG(out, chart, config.getChartWidth(), config.getChartHeight());
        update(out, outputFile); 
    }

    public void renderNormalizedStackedBar(Result result, ChartConfig config, IOOutput outputFile) throws IOException
    {
        JFreeChart chart = new NormalizedStackedBarChartRenderer().render(config, result);
        
        File out = getFile(outputFile); 
        ChartUtilities.saveChartAsPNG(out, chart, config.getChartWidth(), config.getChartHeight());
        update(out, outputFile); 
    }

    public void renderTargetLineOverBarChart(Result result, ChartConfig config, double target, IOOutput outputFile) throws IOException
    {
        JFreeChart chart = new TargetLineOverBarChartRenderer().render(config, result, target);
        
        File out = getFile(outputFile); 
        ChartUtilities.saveChartAsPNG(out, chart, config.getChartWidth(), config.getChartHeight());
        update(out, outputFile); 
    }
    
    private File getFile(IOOutput out)
    	throws IOException
    {
    	if (out instanceof IOFileInfo)
    		return ((IOFileInfo) out).getFile(); 
    	
    	return File.createTempFile("chart", ".tmp"); 
    }
    
    private void update(File tmp, IOOutput out)
    	throws IOException
    {
    	if (out instanceof IOFileInfo)
    		return; 
    	
    	InputStream is = new FileInputStream(tmp); 
    	OutputStream os = out.getOutputStream(); 
    	try { 
    		IOUtils.copy(is, os); 
    	}
    	finally { 
    		IOUtils.closeQuietly(is);
    		IOUtils.closeQuietly(os);
    	}
    }
}
