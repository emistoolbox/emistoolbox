package com.emistoolbox.client.ui.pdf.layout;

import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.TextSet;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.CSSCreator;

public class HtmlPreview 
{
	public static void divOpen(StringBuffer result, String ... namedValues)
	{ tag(result, "div", namedValues); }
	
	public static void tag(StringBuffer result, String tag, String[] namedValues)
	{
		result.append("<"); 
		result.append(tag); 
		attr(result, namedValues); 
		result.append(">"); 
	}
	
	public static void attr(StringBuffer result, String[] namedValues)
	{
		for (int i = 0; i < namedValues.length - 1; i += 2)
			attr(result, namedValues[i], namedValues[i + 1]); 
	}
	
	public static void attr(StringBuffer result, String name, String value)
	{ 
		if (value == null || value.equals(""))
			return; 

		value.replaceAll("'", "\""); 
		
		result.append(' '); 
		result.append(name); 
		result.append("='"); 
		result.append(value); 
		result.append("'"); 
	}

	public static void divClose(StringBuffer result)
	{ result.append("</div>"); } 
	
	public static String getHtmlText(TextSet texts, String key)
	{
		String text = texts.getText(key); 
		if (text == null || text.equals(""))
			return "";
		
		ChartFont font = texts.getFont(key); 
		if (font == null)
			font = ChartFont.DEFAULT_FONT; 
		
		String align = texts.getAlignment(key);
		if (align == null)
			align = PdfText.ALIGN_LEFT;  
				
		return "<div style='text-align: " + align + ";" + getCssText(font) + "'>" + text + "</div>"; 
	}
	
	public static String getCssText(ChartFont font)
	{ return CSSCreator.getCssAsString(font) + "; height: " + (font.getSize() * 15 / 10) + "pt"; }
}
