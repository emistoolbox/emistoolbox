package com.emistoolbox.server.renderer.gis;

import com.emistoolbox.common.renderer.ChartConfig;
import java.io.File;
import java.io.IOException;
import java.util.List;

/** Renders a list of GisFeatureSets into one or more GIS files for display. */ 
public abstract interface GisRenderer
{
    public abstract List<String> getFileNames(); 
    public abstract List<String> getContentTypes();
    
    public abstract void renderMap(List<GisFeatureSet> features, List<ColourScheme> colours, ChartConfig chartConfig, File outputFile) throws IOException;
    public abstract String getColourLegend(ColourScheme paramColourScheme);
    public abstract String getRawColourLegend(ColourScheme paramColourScheme);
    
    public void setValueFormat(String format);
}
