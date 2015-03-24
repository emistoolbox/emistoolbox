package com.emistoolbox.server.renderer.gis.impl;

import com.emistoolbox.server.renderer.gis.ColourLegend;
import com.emistoolbox.server.renderer.gis.ColourScheme;
import com.emistoolbox.server.renderer.gis.GisFeatureSet;
import com.emistoolbox.server.renderer.gis.GisRenderer;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;

/** Common base class for different Gis Renderers. Contains logic on which Entity and label to render without any details on how to render them. 
 */ 
public abstract class GisRendererImpl implements GisRenderer
{
    private String valueFormat;
    
    public void setValueFormat(String valueFormat)
    { this.valueFormat = valueFormat; }
    
    public String getValueFormat()
    { return valueFormat; }
    
    protected abstract void renderFeatureSet(GisFeatureSet features, ColourScheme colours, boolean setRange, boolean showLabels, boolean selectable)
        throws IOException;
    
    protected double[] getBoundary(List<GisFeatureSet> results)
    { 
        double[] boundary = null;
        for (GisFeatureSet feature : results)
            boundary = feature.getBoundary(boundary);
    
        return boundary; 
    }
    
    protected void renderAll(List<GisFeatureSet> results, List<ColourScheme> colours)
        throws IOException
    {
        int finalIndex = results.size() - 1;
        if (hasPoints((GisFeatureSet) results.get(finalIndex)))
            finalIndex--;

        // Place labels on top level with more than 1 feature. 
        // 
        int labelLevel = 0;
        while (labelLevel < results.size() && results.get(labelLevel).getCount() <= 1)
            labelLevel++;
        
        if (labelLevel == results.size())
            labelLevel = 0; 

        for (int i = finalIndex; i >= 0; i--)
            renderFeatureSet((GisFeatureSet) results.get(i), (ColourScheme) colours.get(i), i == results.size() - 1, i == labelLevel, i == results.size() - 1);

        if (finalIndex != results.size() - 1)
            renderFeatureSet((GisFeatureSet) results.get(finalIndex + 1), (ColourScheme) colours.get(finalIndex + 1), true, finalIndex + 1 == labelLevel, true);
    }
    
    public String getRawColourLegend(ColourScheme colours)
    { return getColourLegend(colours); } 
    
    public String getColourLegend(ColourScheme colours)
    {
        List<ColourLegend> legends = colours.getColourLegend();
        if (legends == null)
            return null;

        StringBuilder result = new StringBuilder();
        result.append("<table class='legend'>");
        for (ColourLegend legend : legends)
        {
            result.append("<tr><td class='color' style='background-color: ");
            Color c = legend.colour;
            result.append("#");
            result.append(hex(c.getRGB()));
            result.append("'>&nbsp;</td><td>");
            result.append(legend.text);
            result.append("</td></tr>");
        }

        result.append("</table>");

        return result.toString();
    }

    public static String hex(int i)
    {
        String result = Long.toString(i & 0x00FFFFFF, 16).toUpperCase();
        if (result.length() > 6)
            result = result.substring(result.length() - 6);
        return "000000".substring(result.length(), 6) + result;
    }

    protected boolean hasPoints(GisFeatureSet features)
    {
        if (features.getCount() == 0)
            return false;

        return features.getFeature(0).length == 2;
    }
    
    protected String getBasename(File f)
    {
        String result = f.getName(); 
        int pos = result.lastIndexOf(".");
        if (pos == -1)
            return result; 
        else
            return result.substring(0, pos); 
    }
}
