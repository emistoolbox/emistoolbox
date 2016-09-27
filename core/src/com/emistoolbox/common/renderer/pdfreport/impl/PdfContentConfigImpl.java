package com.emistoolbox.common.renderer.pdfreport.impl;

import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import java.io.Serializable;

public abstract class PdfContentConfigImpl implements PdfContentConfig, Serializable
{
    private String title;

    public String getTitle()
    { return this.title; }

    public void setTitle(String title)
    { this.title = title; }

    public String getInfo()
    { return getTitle(); }
}
