package com.emistoolbox.server.renderer.pdfreport.itext;

import com.emistoolbox.server.renderer.pdfreport.PdfImageContent;
import com.emistoolbox.server.renderer.pdfreport.impl.AbstractPdfContent;

public class ItextPdfImageContent extends AbstractPdfContent implements PdfImageContent
{
    private String imagePath;

    public void setImagePath(String path)
    {
        this.imagePath = path;
    }

    public String getImagePath()
    {
        return this.imagePath;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.server.renderer.pdfreport.itext.ItextPdfImageContent JD-Core
 * Version: 0.6.0
 */