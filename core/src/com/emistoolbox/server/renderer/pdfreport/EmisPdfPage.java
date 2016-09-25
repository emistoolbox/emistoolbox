package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.TextSet;

public interface EmisPdfPage extends TextSet
{
	public static final String[] TEXT_KEYS = new String[] { PdfText.TEXT_FOOTER, PdfText.TEXT_SUBTITLE, PdfText.TEXT_TITLE }; 

	public void setFont(FontIdentifier paramFontIdentifier, ChartFont paramChartFont);
}
