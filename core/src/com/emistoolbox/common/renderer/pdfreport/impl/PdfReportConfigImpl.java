package com.emistoolbox.common.renderer.pdfreport.impl;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.renderer.pdfreport.EmisReportBaseConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfMetaResultContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.util.NamedUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PdfReportConfigImpl extends EmisReportBaseConfigImpl implements PdfReportConfig, Serializable
{
    private int rows = 1;
    private int cols = 1;

    private List<PdfContentConfig> contentConfigs = new ArrayList<PdfContentConfig>();

    public void addContentConfig(PdfContentConfig contentConfig, EmisMetaEntity entityType)
    {
        if (allowContentConfig(contentConfig))
            this.contentConfigs.add(contentConfig);

        if (getEntityType() == null)
            setEntityType(entityType);
    }

    public List<PdfContentConfig> getContentConfigs()
    { return this.contentConfigs; }

    public void setContentConfigs(List<PdfContentConfig> contents)
    { this.contentConfigs = contents; }

    public void setLayout(int rows, int cols)
    {
        this.rows = rows;
        this.cols = cols;
    }

    public int getRows()
    {
        return this.rows;
    }

    public int getColumns()
    {
        return this.cols;
    }
}
