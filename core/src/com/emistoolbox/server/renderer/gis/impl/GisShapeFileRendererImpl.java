package com.emistoolbox.server.renderer.gis.impl;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import shapefile.GisFeatureSetWriter;

import com.emistoolbox.server.renderer.gis.ColourScheme;
import com.emistoolbox.server.renderer.gis.GisFeatureSet;

public class GisShapeFileRendererImpl extends GisMultiFileRendererImpl
{
    private boolean asPoints = false; 
    
    public GisShapeFileRendererImpl()
    { super("", ""); }
    
    @Override
    protected void renderFeatureSet(GisFeatureSet features, ColourScheme colours, boolean setRange, boolean showLabels, boolean selectable) 
            throws IOException
    {
        String filename = getBaseName() + "_layer_" + features.getEntityType().getName(); 

        GisFeatureSetWriter writer = new GisFeatureSetWriter(); 
        writer.write(new File(getDirectory(), filename).getAbsolutePath(), features); 

        asPoints = features.getFeature(0).length <= 2;  
        
        addFileInfo(filename + ".shp", "application/octet-stream");
        addFileInfo(filename + ".shx", "application/octet-stream");
        addFileInfo(filename + ".dbf", "application/octet-stream");
    }

    public String getRawColourLegend(ColourScheme colours)
    {
        if (colours instanceof RangeColourScheme)
            return getRangeColourScheme((RangeColourScheme) colours); 
        
        if (colours instanceof ConstColourScheme)
            return "&colours=" + colours.getFillColour(0.0).getRGB(); 
        
        if (colours instanceof ClassificationColourScheme)
            return getClassificationColourScheme((ClassificationColourScheme) colours); 
        
        return null; 
    }
    
    private String getClassificationColourScheme(ClassificationColourScheme colours)
    { return getThresholdColourScheme(colours.getThresholds(), colours.getColours()); }
    
    private String getThresholdColourScheme(double[] thresholds, Color[] colours)
    {
        StringBuffer result = new StringBuffer(); 
        result.append("&thresholds="); 
        result.append(getDoubleArray(thresholds)); 
        result.append("&colours="); 
        result.append(getColourArray(colours)); 
        result.append("&asPoints="); 
        result.append(asPoints); 

        return result.toString(); 
    }
    
    private String getColourArray(Color[] colors)
    {
        StringBuffer result = new StringBuffer(); 
        for (Color c : colors)
        {
            result.append(","); 
            result.append(GisRendererImpl.hex(c.getRGB()));
        }
        
        return result.substring(1); 
    }
    
    private String getDoubleArray(double[] doubles)
    {
        StringBuffer result = new StringBuffer(); 
        for (double d : doubles)
        {
            result.append(","); 
            result.append(getDouble(d)); 
        }
        
        return result.substring(1); 
    }
    
    private String getRangeColourScheme(RangeColourScheme colours)
    {
        double min = colours.getMin(); 
        double max = colours.getMax(); 

        Color[] cs = colours.getColours(); 
        double[] thresholds = new double[cs.length - 1]; 

        double step = (max - min) / cs.length; 
        for (int i = 0; i < thresholds.length; i++) 
            thresholds[i] = min + step * (i + 1); 
        
        return getThresholdColourScheme(thresholds,  cs); 
    }
    
    private String getDouble(double d)
    {
        if (Double.isNaN(d))
            return "null"; 
        else
            return "" + d; 
    }
}
