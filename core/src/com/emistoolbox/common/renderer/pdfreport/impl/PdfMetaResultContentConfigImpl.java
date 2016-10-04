package com.emistoolbox.common.renderer.pdfreport.impl;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.renderer.pdfreport.PdfMetaResultContentConfig;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultDimensionUtil;
import com.emistoolbox.common.results.MetaResultValue;

import java.io.Serializable;

public abstract class PdfMetaResultContentConfigImpl<T extends MetaResult> extends PdfContentConfigImpl implements PdfMetaResultContentConfig<T>, Serializable
{
    private T metaResult;

    public T getMetaResult()
    { return this.metaResult; }

    public void setMetaResult(T metaResult)
    { this.metaResult = metaResult; }

    public String getInfo()
    { return MetaResultDimensionUtil.getTitle(getMetaResult(), MetaResultDimensionUtil.ENTITY_DATE_LEVEL.GENERIC, false); }

    public EmisMetaEntity getSeniorEntity()
    {
        EmisMetaHierarchy hierarchy = this.metaResult.getHierarchy();
        if (hierarchy == null)
        {
            return null;
        }
        EmisMetaEntity indicatorEntityType = null;
        EmisIndicator indicator = this.metaResult.getIndicator();

        EmisMetaEntity result = null; 
        for (MetaResultValue mrValue : metaResult.getMetaResultValues())
        	result = getSeniorEntity(result, mrValue.getSeniorEntity(hierarchy), hierarchy);  
                
        return getSeniorEntity(result, getSeniorEntity(this.metaResult.getContext(), hierarchy), hierarchy); 
    }
    
    private EmisMetaEntity getSeniorEntity(EmisMetaEntity entity1, EmisMetaEntity entity2, EmisMetaHierarchy hierarchy)
    {
    	if (entity1 == null)
    		return entity2; 
    	
    	if (entity2 == null)
    		return entity1; 

    	return entity1.isChildOf(entity2, hierarchy) ? entity2 : entity1;
    }

    private EmisMetaEntity getSeniorEntity(EmisContext context, EmisMetaHierarchy hierarchy)
    {
        if (context.getEntityType() != null)
        {
            return context.getEntityType();
        }
        if (context.getEntities() == null)
        {
            return null;
        }
        EmisMetaEntity result = null;
        for (EmisEntity entity : context.getEntities())
        {
            EmisMetaEntity entityType = entity.getEntityType();
            if (entityType == null)
            {
                continue;
            }
            if (result == null)
            {
                result = entityType;
            }
            else if (result.isChildOf(entityType, hierarchy))
            {
                result = entityType;
            }
        }

        return result;
    }
}
