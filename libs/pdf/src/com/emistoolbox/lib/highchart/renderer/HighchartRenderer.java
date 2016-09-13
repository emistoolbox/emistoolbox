package com.emistoolbox.lib.highchart.renderer;

import java.io.IOException;

import es.jbauer.lib.io.IOInput;

public interface HighchartRenderer {
	IOInput render (String config,HighchartRenderingType type,double scale,int width) throws IOException, InterruptedException;
}
