package com.emistoolbox.server.renderer.pdfreport.impl;


import com.emistoolbox.server.renderer.charts.ChartRenderer;
import com.emistoolbox.server.renderer.pdfreport.PdfReportWriter;

public abstract class PdfBaseReportWriter implements PdfReportWriter 
{
	private ChartRenderer chartRenderer;
	private boolean debug = false; 
	

	public ChartRenderer getChartRenderer() 
	{ return chartRenderer; }

	public PdfBaseReportWriter setChartRenderer(ChartRenderer chartRenderer) 
	{ 
		this.chartRenderer = chartRenderer; 
		return this; 
	} 

	public PdfBaseReportWriter setDebug(boolean debug)
	{ 
		this.debug = debug;
		return this; 
	}

	public boolean isDebug() 
	{ return debug; }
}
