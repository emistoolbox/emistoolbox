package com.emistoolbox.lib.highchart.renderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.impl.IOFileInput;

public class HighchartPhantomJsFileRenderer extends HighchartPhantomJsRenderer {
	public IOInput render (String config,Map<String,String> parameters,String contentType,File outFile) throws IOException, InterruptedException {
		File inFile = File.createTempFile ("highcharts-in",".js");
		write (config,new FileOutputStream (inFile));
		parameters.put ("infile",inFile.getAbsolutePath ());
		parameters.put ("outfile",outFile.getAbsolutePath ());
		runPhantomJS (parameters).waitFor ();
		inFile.delete ();
		return new IOFileInput (outFile,contentType,null);
	}

	protected boolean rendersAsFile (HighchartRenderingType type) {
		return true;
	}
}
