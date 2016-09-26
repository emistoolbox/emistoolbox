package com.emistoolbox.server.renderer.charts;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.ChartConfig.ChartType;
import com.emistoolbox.common.results.Result;

import es.jbauer.lib.io.IOOutput;

import java.io.IOException;

public abstract interface ChartRenderer
{
	public boolean canCreateContentType(String contentType); 
	
	public void render(ChartType type, Result data, ChartConfig config, IOOutput out)
		throws IOException; 
	
    public abstract void renderBar(Result data, ChartConfig config, IOOutput out) 
    	throws IOException;

    public abstract void renderStackedBar(Result data, ChartConfig config, IOOutput out) 
    	throws IOException;

    public abstract void renderPie(Result data, ChartConfig config, IOOutput out) 
    	throws IOException;
    
    public abstract void renderLines(Result data, ChartConfig config, IOOutput out) 
    	throws IOException; 
    
    public abstract void renderNormalizedStackedBar(Result data, ChartConfig config, IOOutput out) 
    	throws IOException;
}
