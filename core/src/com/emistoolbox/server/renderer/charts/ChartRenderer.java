package com.emistoolbox.server.renderer.charts;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.Result;
import java.io.File;
import java.io.IOException;

public abstract interface ChartRenderer
{
    public abstract void renderBar(Result data, ChartConfig config, File out) 
    	throws IOException;

    public abstract void renderStackedBar(Result data, ChartConfig config, File out) 
    	throws IOException;

    public abstract void renderPie(Result data, ChartConfig config, File out) 
    	throws IOException;
    
    public abstract void renderLines(Result data, ChartConfig config, File out) 
    	throws IOException; 
    
    public abstract void renderNormalizedStackedBar(Result data, ChartConfig config, File out) 
    	throws IOException;
}
