package com.emistoolbox.server.renderer.gis.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.server.renderer.gis.ColourScheme;
import com.emistoolbox.server.renderer.gis.GisFeatureSet;

public abstract class GisMultiFileRendererImpl extends GisRendererImpl
{
    private List<String> filenames = new ArrayList<String>(); 
    private List<String> contentTypes = new ArrayList<String>(); 
    private File directory; 
    private String baseName; 

    private String ext = null; 
    private String contentType = null; 

    public GisMultiFileRendererImpl(String ext, String contentType)
    {
        this.ext = ext; 
        this.contentType = contentType; 
    }

    public List<String> getFileNames()
    { return filenames; } 

    public List<String> getContentTypes()
    { return contentTypes; } 
    
    public void renderMap(List<GisFeatureSet> features, List<ColourScheme> colours, ChartConfig chartConfig, File outputFile) throws IOException
    {
        directory = outputFile.getParentFile(); 
        baseName = getBasename(outputFile);         
        renderAll(features, colours); 
    }
    
    protected String getOutFilename(GisFeatureSet features)
    { return baseName + "_layer_" + features.getEntityType().getName() + ext; }
    
    protected PrintWriter getOut(GisFeatureSet features)
        throws IOException
    {
        String filename = getOutFilename(features);   
        addFileInfo(filename, contentType); 

        return new PrintWriter(new FileOutputStream(new File(directory, filename)));
    }

    protected void addFileInfo(String filename, String contentType)
    {
        filenames.add(filename); 
        contentTypes.add(contentType); 
    } 
    
    protected String getBaseName()
    { return baseName; } 
    
    protected File getDirectory()
    { return directory; } 
}
