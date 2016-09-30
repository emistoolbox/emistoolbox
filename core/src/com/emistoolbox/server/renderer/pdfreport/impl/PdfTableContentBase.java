package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.EmisTableStyle;
import com.emistoolbox.common.renderer.pdfreport.PdfTableContentConfig;
import com.emistoolbox.server.renderer.pdfreport.FontIdentifier;
import com.emistoolbox.server.renderer.pdfreport.PdfContentFontMap;
import com.emistoolbox.server.renderer.pdfreport.PdfTableContent;

public abstract class PdfTableContentBase extends PdfContentBase<PdfTableContentConfig> implements PdfTableContent
{
    private PdfContentFontMap fontMap = new PdfContentFontMap();

    public void setFont(FontIdentifier identifier, ChartFont font)
    { this.fontMap.setFont(identifier, font); }

    public ChartFont getFont(FontIdentifier id)
    { return fontMap.getFont(id); }

	@Override
	public EmisTableStyle getTableStyle() 
	{ 
		if (getConfig() != null)
			return getConfig().getTableStyle(); 

		return null; 
	}

	@Override
	public void setTableStyle(EmisTableStyle style) 
	{} 
}
