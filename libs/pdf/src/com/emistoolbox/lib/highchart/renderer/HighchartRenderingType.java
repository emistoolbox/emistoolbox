package com.emistoolbox.lib.highchart.renderer;

public enum HighchartRenderingType {
	PDF { public String getContentType () { return "application/pdf"; }},
	PNG { public String getContentType () { return "image/png"; }};

	abstract String getContentType ();
	public String getSuffix () { return '.' + toString ().toLowerCase (); }
}
