package com.emistoolbox.lib.highchart.renderer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64InputStream;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.impl.IOFileInput;
import es.jbauer.lib.io.impl.IOInputStreamInput;

public class HighchartPhantomJsPostRenderer extends HighchartPhantomJsRenderer {
	final private static int retryInterval = 100;

	private String host;
	private int port = -1;
	private int timeout = 2000;

	private Process process;

	public String getHost () {
		return host;
	}

	public void setHost (String host) {
		if (!host.equals (this.host)) {
			close ();
			this.host = host;
		}
	}

	public int getPort () {
		return port;
	}

	public void setPort (int port) {
		if (port != this.port) {
			close ();
			this.port = port;
		}
	}

	public int getTimeout () {
		return timeout;
	}

	public void setTimeout (int timeout) {
		this.timeout = timeout;
	}

	public void close () {
		if (process != null) {
			process.destroy ();
			process = null;
		}
	}

	public IOInput render (String config,Map<String,String> parameters,String contentType,File outFile) throws IOException, InterruptedException {
		if (process == null) { // lazy process creation
			Map<String,String> map = new HashMap<String,String> ();
			map.put ("host",host);
			map.put ("port",String.valueOf (port));
			process = runPhantomJS (map);
		}

		URL url = new URL ("http",host,port,"");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection ();
		connection.setRequestMethod ("POST");
		connection.setDoOutput (true);
		connection.setRequestProperty ("ContentType","application/json");

		parameters.put ("infile",config);
		if (outFile != null)
			parameters.put ("outfile",outFile.getAbsolutePath ());

		long start = System.currentTimeMillis ();
		do {
			try {
				write (Util.toJSON (parameters),connection.getOutputStream ());
				InputStream in = connection.getInputStream ();

				if (outFile != null) {
					while (in.read () >= 0) // discard file name returned in request response
						;
					return new IOFileInput (outFile,contentType,null);
				} else
					return new IOInputStreamInput (new Base64InputStream (in,false),null,contentType,null);
			} catch (ConnectException ce) {}
			Thread.sleep (retryInterval);
		} while (System.currentTimeMillis () < start + timeout);

		throw new IOException ("Couldn't connect to Highcharts rendering server after " + timeout / 1000. + " seconds");
	}

	protected boolean rendersAsFile (HighchartRenderingType type) {
		switch (type) {
		case PDF:
		case SVG:
			return true;
		case PNG:
		case JPG:
			return false;
		default:
			throw new InternalError ();
		}
	}
}
