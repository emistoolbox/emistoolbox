package com.emistoolbox.server.renderer.charts;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.Result;
import java.io.File;
import java.io.IOException;

public abstract interface ChartRenderer
{
    public abstract void renderBar(Result paramResult, ChartConfig paramChartConfig, File paramFile) throws IOException;

    public abstract void renderStackedBar(Result paramResult, ChartConfig paramChartConfig, File paramFile) throws IOException;

    public abstract void renderPie(Result paramResult, ChartConfig paramChartConfig, File paramFile) throws IOException;
    
    public abstract void renderLines(Result result, ChartConfig chartconfig, File outFile) throws IOException; 
    
    public abstract void renderNormalizedStackedBar(Result paramResult, ChartConfig paramChartConfig, File paramFile) throws IOException;
}
