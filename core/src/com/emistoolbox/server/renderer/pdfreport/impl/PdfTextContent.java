package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.PdfTextContentConfig;
import com.emistoolbox.server.renderer.pdfreport.PdfContentVisitor;

public class PdfTextContent extends PdfContentBase<PdfTextContentConfig>
{
    private String text;

    private ChartFont titleFont; 
    private ChartFont textFont; 
    
    public PdfTextContent(String title, String text) 
    {
        setTitle(title);
        this.text = text; 
    }

    public void setText(String text)
    { this.text = text; }

    public String getText()
    { return this.text; }
    
    public ChartFont getTitleFont()
    { return titleFont; } 
    
    public ChartFont getTextFont()
    { return textFont; } 
    
    public void setTitleFont(ChartFont font)
    { titleFont = font; } 
    
    public void setTextFont(ChartFont font)
    { textFont = font; }

	@Override
	public <T> T accept(PdfContentVisitor<T> visitor) 
	{ return visitor.visit(this); } 
}
