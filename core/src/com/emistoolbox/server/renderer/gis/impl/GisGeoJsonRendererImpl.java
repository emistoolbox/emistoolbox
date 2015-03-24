package com.emistoolbox.server.renderer.gis.impl;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;

import org.apache.poi.util.IOUtils;

import com.emistoolbox.common.model.meta.GisContext;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.server.renderer.gis.ColourScheme;
import com.emistoolbox.server.renderer.gis.GisFeatureSet;

public class GisGeoJsonRendererImpl extends GisRendererImpl
{
    private PrintWriter out; 
    private List<String> filenames; 
    private GisContext gisContext; 
    private boolean asJavascript = true; 
    
    public GisGeoJsonRendererImpl(GisContext gisContext)
    { this.gisContext = gisContext; }

    public List<String> getFileNames()
    { return filenames; }

    public List<String> getContentTypes()
    { return Arrays.asList("application/json"); }

    public void setAsJavascript(boolean asJavascript)
    { this.asJavascript = asJavascript; }
    
    public void renderMap(List<GisFeatureSet> features, List<ColourScheme> colours, ChartConfig chartConfig, File outputFile) throws IOException
    {
        String filename = getBasename(outputFile) + ".js"; 
        filenames = Arrays.asList(filename);  

        double[] bounds = getBoundary(features);

        out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFile.getParent(), filename))));
        if (asJavascript)
        {
            out.print("var features = new Array();"); 
            renderAll(features, colours);
            out.printf("var bounds = [%.016d, %.016d, %.016d, %.016d];\n", bounds[0], bounds[1], bounds[2], bounds[3]); 
        }
        else
        {
            out.print("{\n    'features' : "); 
            out.println(" ["); 
            renderAll(features, colours);
            out.println("    ], "); 
            out.printf("    'bounds' : [%.016d, %.016d, %.016d, %.016d]", bounds[0], bounds[1], bounds[2], bounds[3]); 
            out.println("}"); 
        }
        
        IOUtils.closeQuietly(out);
    }
    
    private String getBaseLayer()
    { 
        if (gisContext == null)
            return null; 
            
        double[] boundary = gisContext.getBaseLayerBoundary(); 
        int[] size = gisContext.getBaseLayerImageSize(); 
        if (boundary == null || size == null || gisContext.getBaseLayerImage() == null)
            return null ;

        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter.format("['%s', [%.16d, %.16d, %.16d, %.16d], [%i, %i]]", boundary[0], boundary[1], boundary[2], boundary[3], size[0], size[1]); 
        
        return sb.toString();
    }

    private String delim = ""; 
    
    protected void renderAll(List<GisFeatureSet> results, List<ColourScheme> colours) throws IOException
    {
        delim = ""; 
        super.renderAll(results, colours);
    }

    protected void renderFeatureSet(GisFeatureSet features, ColourScheme colours, boolean setRange, boolean showLabels, boolean selectable) 
        throws IOException
    {
        String idPrefix = features.getEntityType().getName() + "_"; 

        try {
            if (asJavascript)
                out.println("  features[features.length] = "); 
            else
                out.print(delim);
            delim = ", "; 
            
            out.println("{"); 
            out.println("    \"type\": \"FeatureCollection\","); 
            out.println("    \"features\": [ "); 
            for (int i = 0; i < features.getCount(); i++) 
            {
                if (i > 0)
                    out.print(", "); 
                outputFeature(out, idPrefix + features.getId(i), features.getFeature(i), features.getValue(i), colours); 
            }
            out.println("    ]"); 

            if (asJavascript)
                out.println("};"); 
            else
                out.println("}"); 
        }
        finally
        { IOUtils.closeQuietly(out); }
    }

    private String getColourName(Color c)
    { return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()); } 
    
    private void outputFeature(PrintWriter out, String id, double[] feature, double value, ColourScheme colours)
    {
        final String indent = "    "; 
        out.println(indent + "{");
        out.println(indent + indent + "\"type\": \"GeometryCollection\","); 
        out.println(indent + indent + "\"id\": \"" + id + "\","); 

        out.println(indent + indent + "\"properties\": {"); 

        if (colours != null)
        {
            Color c = colours.getFillColour(value); 
            if (c != null)
                out.println(indent + indent + indent + "\"emisFillColour\": \"" + getColourName(c) + "\",");
            c = colours.getLineColour(value);
            if (c != null)
                out.println(indent + indent + indent + "\"emisLineColour\": \"" + getColourName(c) + "\",");
        }
        
        if (value != -1)
            out.println(indent + indent + indent + "\"emisValue\": \"" + value + "\","); 
        
        out.println(indent + indent + "},"); 
        outputGeometry(out, feature, indent); 
        out.print(indent + ", "); 
        outputCrs(out, indent); 
        out.println(indent + "}");
        
    }
    
    private void outputGeometry(PrintWriter out, double[] feature, String indent)
    {
        if (feature == null || feature.length < 2)
            return; 
        
        String type = feature.length == 2 ? "Point" : "Polygon"; 
        out.println(indent + indent + "\"geometry\": {");
        out.println(indent + indent + indent + "\"type\": \"" + type + "\","); 
        out.print(indent + indent + indent + "\"coordinates\": ");
        if (feature.length > 2)
            out.println(indent + indent + indent + "[["); 
        
        for (int i = 0; i < feature.length - 1; i += 2)
        {
            if (i > 0)
                out.print(", "); 
            
            out.print("["); 
            out.print(feature[i]); 
            out.print(","); 
            out.print(feature[i + 1]); 
            out.println("]"); 
        }
        
        if (feature.length > 2)
            out.println(indent + indent + indent + " ]]"); 
        
        out.println(indent + indent + "}");
    }
    
    private void outputCrs(PrintWriter out, String indent)
    { out.println(indent + indent + "\"crs\":{\"type\":\"OGC\", \"properties\":{\"urn\":\"urn:ogc:def:crs:OGC:1.3:CRS84\"}}"); }
}
