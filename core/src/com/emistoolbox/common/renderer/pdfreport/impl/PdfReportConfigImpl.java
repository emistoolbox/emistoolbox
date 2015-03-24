package com.emistoolbox.common.renderer.pdfreport.impl;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfMetaResultContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.common.util.impl.NamedImpl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PdfReportConfigImpl extends NamedImpl implements PdfReportConfig, Serializable
{
    private EmisMetaEntity entityType;
    private PdfReportConfig.PageOrientation pageOrientation = PdfReportConfig.PageOrientation.PORTRAIT;
    private PdfReportConfig.PageSize pageSize = PdfReportConfig.PageSize.A4;
    private String footer;
    private String subtitle;
    private int rows = 1;
    private int cols = 1;

    private List<PdfContentConfig> contentConfigs = new ArrayList();

    public EmisMetaEntity getEntityType()
    {
        return this.entityType;
    }

    public String getFooter()
    {
        return this.footer;
    }

    public PdfReportConfig.PageOrientation getOrientation()
    {
        return this.pageOrientation;
    }

    public PdfReportConfig.PageSize getPageSize()
    {
        return this.pageSize;
    }

    public String getSubtitle()
    {
        return this.subtitle;
    }

    public String getTitle()
    {
        return getName();
    }

    public void setEntityType(EmisMetaEntity entityType)
    {
        this.entityType = entityType;
    }

    public void setFooter(String footer)
    {
        this.footer = footer;
    }

    public void setPage(PdfReportConfig.PageSize size, PdfReportConfig.PageOrientation orientation)
    {
        this.pageSize = size;
        this.pageOrientation = orientation;
    }

    public void setTitle(String title, String subtitle)
    {
        setName(title);
        this.subtitle = subtitle;
    }

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

    public EmisMetaHierarchy getHierarchy()
    {
        for (PdfContentConfig contentConfig : this.contentConfigs)
        {
            EmisMetaHierarchy hierarchy = getHierarchy(contentConfig);
            if (hierarchy != null)
            {
                return hierarchy;
            }
        }
        return null;
    }

    private EmisMetaHierarchy getHierarchy(PdfContentConfig contentConfig)
    {
        if (!(contentConfig instanceof PdfMetaResultContentConfig))
        {
            return null;
        }
        MetaResult metaResult = ((PdfMetaResultContentConfig) contentConfig).getMetaResult();
        return metaResult.getHierarchy();
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
    {
        return this.contentConfigs;
    }

    public void setContentConfigs(List<PdfContentConfig> contents)
    {
        this.contentConfigs = contents;
    }

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

    public EmisMetaDateEnum getSeniorDateEnum()
    {
        EmisMetaDateEnum result = null;
        for (PdfContentConfig contentConfig : this.contentConfigs)
        {
            if (!(contentConfig instanceof PdfMetaResultContentConfig))
                continue;

            MetaResult metaResult = ((PdfMetaResultContentConfig) contentConfig).getMetaResult();
            EmisMetaDateEnum tmp = (metaResult == null) || (metaResult.getIndicator() == null) ? null : metaResult.getIndicator().getSeniorDateEnum();
            if (tmp == null)
                continue;

            if ((result == null) || (result.getDimensions() > tmp.getDimensions()))
                result = tmp;
        }
        return result;
    }

    public EmisMetaEntity getSeniorEntity()
    {
        EmisMetaHierarchy hierarchy = getHierarchy();
        if (hierarchy == null)
            return null;

        EmisMetaEntity result = null;
        for (PdfContentConfig contentConfig : this.contentConfigs)
        {
            EmisMetaEntity entityType = contentConfig.getSeniorEntity();
            if (entityType == null)
                continue;

            if (result == null)
                result = entityType;
            else if (result.isChildOf(entityType, hierarchy))
                result = entityType;
        }

        return result;
    }
}
