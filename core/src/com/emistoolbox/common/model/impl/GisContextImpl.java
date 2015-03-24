package com.emistoolbox.common.model.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.common.model.meta.GisContext;
import com.emistoolbox.common.model.meta.GisLayer;

public class GisContextImpl implements GisContext, Serializable
{
    private String projection; 
    private String baseImage; 
    private double[] baseImageBoundary; 
    private int[] baseImageSize; 
    private List<GisLayer> layers = new ArrayList<GisLayer>(); 
    
    public String getProjection()
    { return projection; }

    public void setProjection(String proj)
    { this.projection = proj; } 

    public String getBaseLayerImage()
    { return baseImage; }

    public void setBaseLayerImage(String name)
    { this.baseImage = name; } 

    public double[] getBaseLayerBoundary()
    { return baseImageBoundary; } 

    public void setBaseLayerBoundary(double[] boundary)
    { this.baseImageBoundary = boundary; }

    public int[] getBaseLayerImageSize()
    { return baseImageSize; }

    public void setBaseLayerImageSize(int[] size)
    { this.baseImageSize = size; }

    public void addGisLayer(GisLayer layer)
    { layers.add(layer); } 

    public List<GisLayer> getGisLayers()
    { return layers; }

    public void setGisLayers(List<GisLayer> layers)
    { this.layers = layers; }

	@Override
	public boolean hasAnyValues() 
	{
		if (layers != null && layers.size() > 0)
			return true; 

		if (hasValue(projection) || hasValue(baseImage))
			return true; 

		if (baseImageBoundary != null && baseImageBoundary.length > 0)
			return true; 

		if (baseImageSize != null && baseImageSize.length > 0)
			return true; 

		return false;
	} 
	
	private boolean hasValue(String value)
	{ return value != null && !value.equals(""); }
}
