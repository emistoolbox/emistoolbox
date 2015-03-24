package com.emistoolbox.server.renderer.pdfreport;


import com.emistoolbox.common.ChartFont;

import java.util.HashMap;

import java.util.Map;


public class PdfContentFontMap

{
    private Map<FontIdentifier, ChartFont> fontMap = new HashMap();
    
    public PdfContentFontMap() 
    { this.fontMap.put(FontIdentifier.DEFAULT, new ChartFont("SANS-SERIF", 0, 8)); }

    public void setFont(FontIdentifier identifier, ChartFont font)
    { this.fontMap.put(identifier, font); }

    public ChartFont getFont(FontIdentifier identifier)
    { return this.fontMap.containsKey(identifier) ? (ChartFont) this.fontMap.get(identifier) : (ChartFont) this.fontMap.get(FontIdentifier.DEFAULT); }
}
