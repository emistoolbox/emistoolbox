package com.emistoolbox.server.renderer.pdfreport.fonts;

import com.emistoolbox.common.ChartFont;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.FontMapper;

public class FontUtils
{
    public static final FontMapper mapper = new DefaultFontMapper();

    public static com.itextpdf.text.Font getItextFont(java.awt.Font font)
    {
        return new com.itextpdf.text.Font(mapper.awtToPdf(font));
    }

    public static com.itextpdf.text.Font getItextFont(ChartFont font)
    {
        return new com.itextpdf.text.Font(mapper.awtToPdf(getFont(font)));
    }

    public static java.awt.Font getFont(ChartFont chartFont)
    {
        return new java.awt.Font(chartFont.getName(), chartFont.getStyle(), chartFont.getSize());
    }
}
