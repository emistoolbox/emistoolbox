package com.emistoolbox.server.renderer.charts.impl;

import java.io.IOException;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.ChartConfig.ChartType;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.renderer.charts.ChartRenderer;

import es.jbauer.lib.io.IOOutput;

public abstract class BaseChartRenderer implements ChartRenderer 
{
	@Override
	public void render(ChartType type, Result data, ChartConfig config, IOOutput out) throws IOException {
		switch (type)
		{
		case BAR: 
			renderBar(data,  config, out);
			break; 
			
		case STACKED: 
			renderStackedBar(data,  config, out);
			break; 
			
		case PIE: 
			renderPie(data,  config, out);
			break; 
			
		case STACKED_SCALED: 
			renderNormalizedStackedBar(data,  config, out);
			break; 
			
		case LINE: 
			renderLines(data, config, out);
			break; 
		}
	}
}
