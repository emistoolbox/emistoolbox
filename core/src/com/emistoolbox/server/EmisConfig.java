package com.emistoolbox.server;

public class EmisConfig
{
	public static final String EMISTOOLBOX_PATH = "emistoolbox.path"; 
	public static final String EMISTOOLBOX_PATH_WRITABLE = "emistoolbox.path.writable";

	public static final String EMISTOOLBOX_RENDERER_CHART = "emistoolbox.renderer.chart"; 
	public static final String EMISTOOLBOX_RENDERER_PDF= "emistoolbox.renderer.pdf"; 

	public static final String RENDERER_CHART_JFREECHART = "jfree"; 
	public static final String RENDERER_CHART_HIGHCHART = "highcharts"; 

	public static final String RENDERER_PDF_ITEXT = "itext"; 
	public static final String RENDERER_PDF_JORIKI = "joriki"; 

	public static final String EMISTOOLBOX_PHANTOMJS_SERVER = "phantomjs.host"; 
	public static final String EMISTOOLBOX_PHANTOMJS_PORT = "phantomjs.port"; 

	public static String get(String name, String defaultValue)
    {
        String result = System.getProperty(name);
        if (result == null)
            return defaultValue;

        return result;
    }
}
