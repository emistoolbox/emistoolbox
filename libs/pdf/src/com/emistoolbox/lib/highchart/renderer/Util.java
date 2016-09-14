package com.emistoolbox.lib.highchart.renderer;

import java.io.File;
import java.util.Map;
import java.util.StringTokenizer;

public class Util {
	// directory is a locally valid directory path string
	// path is relative to directory and delimited by slashes
	public static String getAbsolutePath (String directory,String path) {
		StringTokenizer tok = new StringTokenizer (path,"/");
		File file = new File (directory);
		while (tok.hasMoreTokens ())
			file = new File (file,tok.nextToken ());
		return file.getAbsolutePath ();
	}

	public static String toJSON (Map<String,String> map) {
		StringBuilder builder = new StringBuilder ();
		builder.append ('{');
		boolean first = true;
		for (Map.Entry<String,String> entry : map.entrySet ()) {
			if (first)
				first = false;
			else
				builder.append (',');
			builder
				.append ('"')
				.append (entry.getKey ())
				.append ('"')
				.append (':')
				.append ('"')
				.append (entry.getValue ().replace ("\\","\\\\").replace ("\"","\\\""))
				.append ('"');
		}
		builder.append ('}');
		return builder.toString ();
	}
}
