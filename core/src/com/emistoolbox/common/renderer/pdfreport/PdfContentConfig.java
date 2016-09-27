package com.emistoolbox.common.renderer.pdfreport;

import com.emistoolbox.common.model.meta.EmisMetaEntity;

public abstract interface PdfContentConfig
{
    public abstract void setTitle(String paramString);
    public abstract String getTitle();

    public abstract String getInfo();
    public abstract EmisMetaEntity getSeniorEntity();
}
