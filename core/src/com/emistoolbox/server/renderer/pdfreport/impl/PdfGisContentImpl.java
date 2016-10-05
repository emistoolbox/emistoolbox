package com.emistoolbox.server.renderer.pdfreport.impl;

import java.util.List;

import com.emistoolbox.common.model.meta.GisContext;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfGisContentConfig;
import com.emistoolbox.server.renderer.gis.ColourScheme;
import com.emistoolbox.server.renderer.gis.GisFeatureSet;
import com.emistoolbox.server.renderer.pdfreport.PdfContentVisitor;
import com.emistoolbox.server.renderer.pdfreport.PdfGisContent;

import info.joriki.graphics.Rectangle;

public class PdfGisContentImpl extends PdfContentBase<PdfGisContentConfig> implements PdfGisContent 
{
	private List<GisFeatureSet> features; 
	private ChartConfig chartConfig; 
	private List<ColourScheme> colourSchemes; 
	private ColourScheme legendColourScheme; 
	private String mapType; 
	private String format; 
	private GisContext gisContext; 
	
	@Override
	public <T> T accept(PdfContentVisitor<T> visitor) 
	{ return visitor.visit(this); }

	@Override
	public List<GisFeatureSet> getFeatures() 
	{ return features; } 

	@Override
	public void setFeatures(List<GisFeatureSet> features) 
	{ this.features = features; } 

	@Override
	public String getMapType() 
	{ return mapType; } 

	@Override
	public void setMapType(String mapType) 
	{ this.mapType = mapType; } 

	@Override
	public List<ColourScheme> getColourSchemes() 
	{ return colourSchemes; } 

	@Override
	public void setColourSchemes(List<ColourScheme> colourSchemes) 
	{ this.colourSchemes = colourSchemes; } 

	@Override
	public ColourScheme getLegendColourScheme() 
	{ return legendColourScheme; }

	@Override
	public void setLegendColourScheme(ColourScheme colourScheme) 
	{ this.legendColourScheme = legendColourScheme; }

	@Override
	public String getFormat() 
	{ return format; } 

	@Override
	public void setFormat(String format) 
	{ this.format = format; } 

	@Override
	public ChartConfig getChartConfig() 
	{ return chartConfig; } 

	@Override
	public void setChartConfig(ChartConfig config) 
	{ this.chartConfig = chartConfig; }

	@Override
	public GisContext getGisContext() 
	{ return gisContext; } 

	@Override
	public void setGisContext(GisContext gisContext) 
	{ this.gisContext = gisContext; } 
}
