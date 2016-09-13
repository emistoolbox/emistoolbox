package com.emistoolbox.lib.highchart.renderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.impl.IOFileInput;

public class HighchartPhantomJsRenderer implements HighchartRenderer {
	String phantomJSDirectory;
	String highchartsExportServerDirectory;
	
	public IOInput render (String config,HighchartRenderingType type,double scale,int width) throws IOException, InterruptedException {
		File inFile = File.createTempFile ("highcharts-in",".js");
		File outFile = File.createTempFile ("highcharts-out",type.getSuffix ());
		
		OutputStream out = new FileOutputStream (inFile);
		out.write (config.getBytes ("UTF-8"));
		out.close ();
		
		Runtime.getRuntime ().exec (new String [] {
				Util.getAbsolutePath (phantomJSDirectory,"bin/phantomjs"),
				Util.getAbsolutePath (highchartsExportServerDirectory,"java/highcharts-export/highcharts-export-convert/src/main/resources/phantomjs/highcharts-convert.js"),
				"-infile",inFile.getAbsolutePath (),
				"-outfile",outFile.getAbsolutePath (),
				"-type",type.toString (),
				"-scale",String.valueOf (scale),
				"-width",String.valueOf (width)
		}).waitFor ();

		inFile.delete ();
		outFile.deleteOnExit ();
		
		return new IOFileInput (outFile,type.getContentType (),null);
	}

	public String getPhantomJSDirectory () {
		return phantomJSDirectory;
	}

	public void setPhantomJSDirectory (String phantomJSDirectory) {
		this.phantomJSDirectory = phantomJSDirectory;
	}

	public String getHighchartsExportServerDirectory () {
		return highchartsExportServerDirectory;
	}

	public void setHighchartsExportServerDirectory (String highchartsExportServerDirectory) {
		this.highchartsExportServerDirectory = highchartsExportServerDirectory;
	}

	// example usage
	
	final static String configString = "{title: {text:\"Smørrebrød\"},xAxis: {categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']},series: [{data: [29.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4]}]}";

	public static void main (String [] args) throws IOException, InterruptedException {
		HighchartPhantomJsRenderer renderer = new HighchartPhantomJsRenderer ();
		renderer.setPhantomJSDirectory ("/Users/joriki/work/Jörg/highcharts/phantomjs-2.1.1-macosx");
		renderer.setHighchartsExportServerDirectory ("/Users/joriki/work/Jörg/highcharts/highcharts-export-server");
		IOInput input = renderer.render (configString,HighchartRenderingType.PDF,2.5,1000);
		byte [] b = new byte [8];
		input.getInputStream ().read (b);
		System.out.println (new String (b)); // should print "%PDF-1.4"
	}
}
