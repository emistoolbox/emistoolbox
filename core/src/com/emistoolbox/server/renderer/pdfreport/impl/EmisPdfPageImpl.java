package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.TextSetImpl;
import com.emistoolbox.server.renderer.pdfreport.EmisPdfPage;
import com.emistoolbox.server.renderer.pdfreport.FontIdentifier;
import com.emistoolbox.server.renderer.pdfreport.PdfContentFontMap;

public class EmisPdfPageImpl extends TextSetImpl implements EmisPdfPage 
{
    private PdfContentFontMap fontMap = new PdfContentFontMap();

    public EmisPdfPageImpl()
    { super(TEXT_KEYS); } 
    
    @Override
    public void setFont(FontIdentifier identifier, ChartFont font)
    { this.fontMap.setFont(identifier, font); }
}
