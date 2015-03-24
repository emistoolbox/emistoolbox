package com.emistoolbox.common.renderer.pdfreport.impl;

import java.io.Serializable;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.renderer.pdfreport.PdfTextContentConfig;

public class PdfTextContentConfigImpl extends PdfContentConfigImpl implements PdfTextContentConfig, Serializable
{
    private String text;

    public String getText()
    { return text; }

    public void setText(String text)
    { this.text = text; }

    public EmisMetaEntity getSeniorEntity()
    { return null; }

}
