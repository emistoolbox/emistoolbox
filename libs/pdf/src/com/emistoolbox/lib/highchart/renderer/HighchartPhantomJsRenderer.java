package com.emistoolbox.lib.highchart.renderer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import es.jbauer.lib.io.IOInput;

abstract public class HighchartPhantomJsRenderer implements HighchartRenderer {
	protected String phantomJSDirectory;
	protected String highchartsExportServerDirectory;

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

	protected Process runPhantomJS (Map<String,String> parameters) throws InterruptedException, IOException {
		String [] command = new String [2 + 2 * parameters.size ()];

		int i = 0;
		command [i++] = Util.getAbsolutePath (phantomJSDirectory,"bin/phantomjs");
		command [i++] = Util.getAbsolutePath (highchartsExportServerDirectory,"java/highcharts-export/highcharts-export-convert/src/main/resources/phantomjs/highcharts-convert.js");
		for (Map.Entry<String,String> entry : parameters.entrySet ()) {
			command [i++] = '-' + entry.getKey ();
			command [i++] = entry.getValue ();
		}

		return Runtime.getRuntime ().exec (command);
	}

	protected void write (String string,OutputStream out) throws IOException {
		out.write (string.getBytes ("UTF-8"));
		out.close ();
	}

	public IOInput render (String config,HighchartRenderingType type,double scale,int width) throws IOException, InterruptedException {
		Map<String,String> parameters = new HashMap<String,String> ();
		parameters.put ("type",type.toString ());
		if (width > 0)
			parameters.put ("width",String.valueOf (width));
		else
			parameters.put ("scale",String.valueOf (scale));

		File outFile = null;
		if (rendersAsFile (type)) {
			outFile = File.createTempFile ("highcharts-out",type.getSuffix ());
			outFile.deleteOnExit ();
		}

		return render (config,parameters,type.getContentType (),outFile);
	}

	abstract public IOInput render (String config,Map<String,String> parameters,String contentType,File outFile) throws IOException, InterruptedException;
	abstract protected boolean rendersAsFile (HighchartRenderingType type);

	// example usage
	
	final static String configString = "{title: {text:\"Smørrebrød\"},xAxis: {categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']},series: [{data: [29.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4]}]}";
	final static HighchartRenderingType type = HighchartRenderingType.SVG;
	final static boolean useFileRenderer = true;

	public static void main (String [] args) throws IOException, InterruptedException {
		HighchartPhantomJsRenderer renderer;

		if (useFileRenderer)
			renderer = new HighchartPhantomJsFileRenderer ();
		else {
			HighchartPhantomJsPostRenderer postRenderer = new HighchartPhantomJsPostRenderer ();
			postRenderer.setHost ("127.0.0.1");
			postRenderer.setPort (3003);
			renderer = postRenderer;
		}

		renderer.setPhantomJSDirectory ("/Users/joriki/work/Jörg/highcharts/phantomjs-2.1.1-macosx");
		renderer.setHighchartsExportServerDirectory ("/Users/joriki/work/Jörg/highcharts/highcharts-export-server");

		IOInput input = renderer.render (configString,type,2.5,0);
	}
}