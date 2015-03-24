package com.emistoolbox.common.model.meta;

import java.util.List;

public interface GisContext
{
	public boolean hasAnyValues(); 
	
    public String getProjection(); 
    public void setProjection(String proj); 
    
    public String getBaseLayerImage(); 
    public void setBaseLayerImage(String name); 
    
    public int[] getBaseLayerImageSize(); 
    public void setBaseLayerImageSize(int[] size); 

    public double[] getBaseLayerBoundary(); 
    public void setBaseLayerBoundary(double[] boundary);
    
    public void addGisLayer(GisLayer layer); 
    public List<GisLayer> getGisLayers();
    public void setGisLayers(List<GisLayer> layers); 
}
