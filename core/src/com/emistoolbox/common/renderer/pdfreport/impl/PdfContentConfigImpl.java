package com.emistoolbox.common.renderer.pdfreport.impl;

import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import java.io.Serializable;

public abstract class PdfContentConfigImpl implements PdfContentConfig, Serializable
{
    private String title;

    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getInfo()
    {
        return getTitle();
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.common.renderer.pdfreport.impl.PdfContentConfigImpl JD-Core
 * Version: 0.6.0
 */