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

    public boolean allowContentConfig(PdfContentConfig contentConfig)
    {
        if (getEntityType() == null)
        {
            return true;
        }
        if (!(contentConfig instanceof PdfMetaResultContentConfig))
        {
            return true;
        }
        MetaResult metaResult = ((PdfMetaResultContentConfig) contentConfig).getMetaResult();
        EmisMetaEntity contentConfigEntity = contentConfig.getSeniorEntity();

        EmisMetaHierarchy currentHierarchy = getHierarchy();
        if (currentHierarchy != null)
        {
            EmisMetaHierarchy contentHierarchy = getHierarchy(contentConfig);
            if ((contentHierarchy != null) && (!NamedUtil.sameName(contentHierarchy, currentHierarchy)))
            {
                return false;
            }
        }

        return (contentConfigEntity != null) && (!getEntityType().isChildOf(contentConfigEntity, metaResult.getHierarchy()));
    }

    public void addContentConfig(PdfContentConfig contentConfig, EmisMetaEntity entityType)
    {
        if (allowContentConfig(contentConfig))
        {
            this.contentConfigs.add(contentConfig);
        }
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
