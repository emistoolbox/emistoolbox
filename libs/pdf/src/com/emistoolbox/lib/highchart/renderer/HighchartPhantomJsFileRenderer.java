package com.emistoolbox.lib.highchart.renderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.impl.IOFileInput;

public class HighchartPhantomJsFileRenderer extends HighchartPhantomJsRenderer {
	protected String phantomJSDirectory;
	protected String highchartsDirectory;

	public HighchartPhantomJsFileRenderer()
	{}
	
	public HighchartPhantomJsFileRenderer(String phantomJSDirectory, String highchartsDirectory)
	{
		this.phantomJSDirectory = phantomJSDirectory; 
		this.highchartsDirectory = highchartsDirectory; 
	}
	
	public String getPhantomJSDirectory () {
		return phantomJSDirectory;
	}

	public void setPhantomJSDirectory (String phantomJSDirectory) {
		this.phantomJSDirectory = phantomJSDirectory;
	}

	public String getHighchartsDirectory () {
		return highchartsDirectory;
	}

	public void setHighchartsDirectory (String highchartsDirectory) {
		this.highchartsDirectory = highchartsDirectory;
	}

	private Process runPhantomJS (Map<String,String> parameters) throws InterruptedException, IOException {
		String [] command = new String [2 + 2 * parameters.size ()];

		int i = 0;
		command [i++] = Util.getAbsolutePath (phantomJSDirectory,"bin/phantomjs");
		command [i++] = Util.getAbsolutePath (highchartsDirectory,"highcharts-convert.js");
		for (Map.Entry<String,String> entry : parameters.entrySet ()) {
			command [i++] = '-' + entry.getKey ();
			command [i++] = entry.getValue ();
		}

		return Runtime.getRuntime ().exec (command);
	}

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
