package com.emistoolbox.common.renderer.pdfreport;

import java.io.Serializable;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.util.impl.NamedImpl;

public abstract class EmisReportBaseConfigImpl extends TextSetImpl implements EmisPdfReportConfig, Serializable
{
	public  static final String[] TEXT_KEYS = new String[] { PdfText.TEXT_TITLE, PdfText.TEXT_SUBTITLE, PdfText.TEXT_FOOTER };
	
    private EmisMetaEntity entityType;
    private PdfReportConfig.PageOrientation pageOrientation = PdfReportConfig.PageOrientation.PORTRAIT;
    private PdfReportConfig.PageSize pageSize = PdfReportConfig.PageSize.A4;

    private boolean shortTitles;

    public EmisReportBaseConfigImpl()
    { super(TEXT_KEYS); } 
    
    @Override
	public String getName() 
    { return getText(PdfText.TEXT_TITLE); }

	@Override
	public void setName(String value) 
	{ putText(PdfText.TEXT_TITLE, value); }

	public EmisMetaEntity getEntityType()
    { return this.entityType; }

    public PdfReportConfig.PageOrientation getOrientation()
    { return this.pageOrientation; }

    public PdfReportConfig.PageSize getPageSize()
    { return this.pageSize; }

    public void setEntityType(EmisMetaEntity entityType)
    { this.entityType = entityType; }

    public void setPage(PdfReportConfig.PageSize size, PdfReportConfig.PageOrientation orientation)
    {
        this.pageSize = size;
        this.pageOrientation = orientation;
    }

	@Override
	public void setShortTitles(boolean shortTitles) 
	{ this.shortTitles = shortTitles; } 

	@Override
	public boolean hasShortTitles() 
	{ return shortTitles; } 


    public EmisMetaDateEnum getSeniorDateEnum()
    {
        EmisMetaDateEnum result = null;
        for (PdfContentConfig contentConfig : getContentConfigs())
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
        for (PdfContentConfig contentConfig : getContentConfigs())
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

    public EmisMetaHierarchy getHierarchy()
    {
        for (PdfContentConfig contentConfig : getContentConfigs())
        {
            EmisMetaHierarchy hierarchy = getHierarchy(contentConfig);
            if (hierarchy != null)
            {
                return hierarchy;
            }
        }
        return null;
    }

    protected EmisMetaHierarchy getHierarchy(PdfContentConfig contentConfig)
    {
        if (!(contentConfig instanceof PdfMetaResultContentConfig))
            return null;
    
        MetaResult metaResult = ((PdfMetaResultContentConfig) contentConfig).getMetaResult();
        return metaResult.getHierarchy();
    }
}