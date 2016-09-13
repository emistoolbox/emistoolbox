package com.emistoolbox.lib.highchart.renderer;

public enum HighchartRenderingType {
	PDF { public String getContentType () { return "application/pdf"; }},
	PNG { public String getContentType () { return "image/png"; }};

	public abstract String getContentType ();
	public String getSuffix () { return '.' + toString ().toLowerCase (); }
}
