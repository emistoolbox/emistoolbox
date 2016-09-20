package com.emistoolbox.common.renderer.pdfreport;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.Named;
import java.util.List;

public abstract interface PdfReportConfig extends Named
{
    public abstract EmisMetaEntity getEntityType();

    public abstract void setEntityType(EmisMetaEntity paramEmisMetaEntity);

    public abstract void setPage(PageSize paramPageSize, PageOrientation paramPageOrientation);

    public abstract PageSize getPageSize();

    public abstract PageOrientation getOrientation();

    public abstract void setTitle(String paramString1, String paramString2);

    public abstract String getTitle();

    public abstract String getSubtitle();

    public abstract void setFooter(String paramString);

    public abstract String getFooter();

    public abstract boolean allowContentConfig(PdfContentConfig paramPdfContentConfig);

    public abstract void addContentConfig(PdfContentConfig paramPdfContentConfig, EmisMetaEntity paramEmisMetaEntity);

    public abstract List<PdfContentConfig> getContentConfigs();

    public abstract void setContentConfigs(List<PdfContentConfig> paramList);

    public abstract void setLayout(int paramInt1, int paramInt2);

    public abstract int getRows();

    public abstract int getColumns();

    public abstract EmisMetaDateEnum getSeniorDateEnum();

    public abstract EmisMetaEntity getSeniorEntity();

    public abstract EmisMetaHierarchy getHierarchy();
    
    public void setShortTitles(boolean shortTitles); 
    
    public boolean hasShortTitles(); 
    
    public static enum PageOrientation {
        LANDSCAPE, PORTRAIT;
    }

    public static enum PageSize {
        A4, A5, LETTER;
    }
}
