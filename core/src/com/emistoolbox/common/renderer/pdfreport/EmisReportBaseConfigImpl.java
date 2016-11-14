package com.emistoolbox.common.renderer.pdfreport;

import java.io.Serializable;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.common.util.impl.NamedImpl;

public abstract class EmisReportBaseConfigImpl extends TextSetImpl implements EmisPdfReportConfig, Serializable
{
	public  static final String[] TEXT_KEYS = new String[] { PdfText.TEXT_TITLE, PdfText.TEXT_SUBTITLE, PdfText.TEXT_FOOTER };
	
    private EmisMetaEntity entityType;
    private PdfReportConfig.PageOrientation pageOrientation = PdfReportConfig.PageOrientation.PORTRAIT;
    private PdfReportConfig.PageSize pageSize = PdfReportConfig.PageSize.A4;

    private boolean shortTitles;
    private String name; 
    
    public EmisReportBaseConfigImpl()
    { super(TEXT_KEYS); } 
    
    @Override
	public String getName() 
    { return name; }

	@Override
	public void setName(String name) 
	{ this.name = name; }

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

            if (result == null || result.getDimensions() > tmp.getDimensions())
            	result = tmp;
        }
        
        return result;
    }

    public EmisMetaEntity getJuniorEntity()
    { return getEntity(false); }
    
    public EmisMetaEntity getSeniorEntity()
    { return getEntity(true); } 
    
    public EmisMetaEntity getEntity(boolean senior)
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
            else 
        	{
            	if (senior)
            	{
	            	if (result.isChildOf(entityType, hierarchy))
	                    result = entityType;
            	}
            	else
            	{
            		if (entityType.isChildOf(result, hierarchy))
            			result = entityType; 
            	}
        	}
        }

        return result;
    }

    public EmisMetaHierarchy getHierarchy()
    {
        for (PdfContentConfig contentConfig : getContentConfigs())
        {
            EmisMetaHierarchy hierarchy = getHierarchy(contentConfig);
            if (hierarchy != null)
                return hierarchy;
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

    public boolean allowContentConfig(PdfContentConfig contentConfig)
    {
    	if (contentConfig instanceof PdfPriorityListContentConfig)
    		// PriorityList content config can always be added.  
    		return true; 
    	
        if (getEntityType() == null)
        	// Report doesn't have an entity type - we allow anything. 
            return true;

        if (!(contentConfig instanceof PdfMetaResultContentConfig))
            return true;

        MetaResult metaResult = ((PdfMetaResultContentConfig) contentConfig).getMetaResult();
        EmisMetaEntity contentConfigEntity = contentConfig.getSeniorEntity();

        EmisMetaHierarchy currentHierarchy = getHierarchy();
        if (currentHierarchy != null)
        {
            EmisMetaHierarchy contentHierarchy = getHierarchy(contentConfig);
            if ((contentHierarchy != null) && (!NamedUtil.sameName(contentHierarchy, currentHierarchy)))
                return false;
        }
        
        return contentConfigEntity != null && !contentConfigEntity.isChildOf(getEntityType(), metaResult.getHierarchy());
    }
}
