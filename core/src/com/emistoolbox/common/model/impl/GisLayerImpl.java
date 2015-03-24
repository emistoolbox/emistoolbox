package com.emistoolbox.common.model.impl;

import java.io.Serializable;

import com.emistoolbox.common.model.meta.GisLayer;

public class GisLayerImpl implements GisLayer, Serializable
{
    private String name; 
    private String path; 
    private String style; 
    
    public String getName()
    { return name; }

    public void setName(String name)
    { this.name = name; } 

    public String getPath()
    { return path; }

    public void setPath(String path)
    { this.path = path; }

    public String getStyle()
    { return style; } 

    public void setStyle(String style)
    { this.style = style; } 
}

