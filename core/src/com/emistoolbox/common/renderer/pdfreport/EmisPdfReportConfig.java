package com.emistoolbox.common.renderer.pdfreport;

import java.util.List;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.Named;

public interface EmisPdfReportConfig extends Named, TextSet
{
	public EmisMetaEntity getEntityType();
    public void setEntityType(EmisMetaEntity paramEmisMetaEntity);

    public void setPage(PageSize paramPageSize, PageOrientation paramPageOrientation);
    public PageSize getPageSize();
    public PageOrientation getOrientation();
    
    public List<PdfContentConfig> getContentConfigs(); 
    
    public EmisMetaDateEnum getSeniorDateEnum();
    public EmisMetaEntity getSeniorEntity();

    public EmisMetaHierarchy getHierarchy();
    
    public void setShortTitles(boolean shortTitles); 
    public boolean hasShortTitles(); 
    
    public static enum PageOrientation 
    { LANDSCAPE, PORTRAIT; }

    public static enum PageSize 
    { A4, A5, LETTER; }
    
    public boolean allowContentConfig(PdfContentConfig content);
}
