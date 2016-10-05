package com.emistoolbox.server.renderer.pdfreport;

import java.util.List;

import com.emistoolbox.common.model.meta.GisContext;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfGisContentConfig;
import com.emistoolbox.server.renderer.gis.ColourScheme;
import com.emistoolbox.server.renderer.gis.GisFeatureSet;

public interface PdfGisContent extends PdfContent<PdfGisContentConfig> 
{
	List<GisFeatureSet> getFeatures(); 
	void setFeatures(List<GisFeatureSet> features); 
	
	String getMapType(); 
	void setMapType(String mapType); 
	
	List<ColourScheme> getColourSchemes(); 
	void setColourSchemes(List<ColourScheme> colourSchemes); 
	
	ColourScheme getLegendColourScheme(); 
	void setLegendColourScheme(ColourScheme colourScheme); 
	
	String getFormat(); 
	void setFormat(String format); 
	
	ChartConfig getChartConfig(); 
	void setChartConfig(ChartConfig config); 
	
	GisContext getGisContext(); 
	void setGisContext(GisContext gisContext); 
}
