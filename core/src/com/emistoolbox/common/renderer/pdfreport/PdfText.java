package com.emistoolbox.common.renderer.pdfreport;

import com.emistoolbox.common.ChartFont;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFont;

public class PdfText 
{
	public static final String TEXT_TITLE = "title";
	public static final String TEXT_SUBTITLE = "subtitle";
	public static final String TEXT_PLAIN = "plain";
	public static final String TEXT_FOOTER = "footer";

	public final static ChartFont DEFAULT_FONT_HACK = new ChartFont(PDFLayoutFont.FONT_HELVETICA, ChartFont.PLAIN, 12);  
	public final static ChartFont DEFAULT_TITLE_FONT_HACK = new ChartFont(PDFLayoutFont.FONT_HELVETICA, ChartFont.BOLD, 14);  
}
