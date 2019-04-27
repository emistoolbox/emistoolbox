package com.emistoolbox.common.model.impl;

import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.model.meta.GisContext;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.NamedUtil;
import java.io.Serializable;

public class EmisMetaImpl implements EmisMeta, Serializable
{
    private static final long serialVersionUID = 1L;
    private NamedIndexList<EmisMetaDateEnum> dates = new NamedIndexList<EmisMetaDateEnum>();
    private NamedIndexList<EmisMetaEntity> entities = new NamedIndexList<EmisMetaEntity>();
    private NamedIndexList<EmisMetaEnum> enums = new NamedIndexList<EmisMetaEnum>();
    private NamedIndexList<EmisMetaHierarchy> hierarchies = new NamedIndexList<EmisMetaHierarchy>();
    private GisContext gisContext;
    private transient String datasetName; 

    public String getDatasetName()
    { return datasetName; } 
    
    public void setDatasetName(String name)
    { datasetName = name; } 
    
    public NamedIndexList<EmisMetaDateEnum> getDateEnums()
    { return this.dates; }

    public NamedIndexList<EmisMetaEntity> getEntities()
    { return this.entities; }

    public NamedIndexList<EmisMetaEnum> getEnums()
    { return this.enums; }

    public NamedIndexList<EmisMetaHierarchy> getHierarchies()
    { return this.hierarchies; }

    public void setDateEnums(NamedIndexList<EmisMetaDateEnum> dateEnums)
    { this.dates = dateEnums; }

    public void setEntities(NamedIndexList<EmisMetaEntity> entities)
    { this.entities = entities; }

    public void setEnums(NamedIndexList<EmisMetaEnum> enums)
    { this.enums = enums; }

    public void setHierarchies(NamedIndexList<EmisMetaHierarchy> hierarchies)
    { this.hierarchies = hierarchies; }

    public EmisMetaDateEnum getDefaultDateType()
    {
        EmisMetaDateEnum result = null;
        for (EmisMetaDateEnum dateType : getDateEnums())
        {
            if (result == null)
            {
                result = dateType;
                continue;
            }

            if (dateType.getSize() < result.getSize())
                result = dateType;
        }
        return result;
    }

    public int getDefaultDateTypeIndex()
    { return NamedUtil.findIndex(getDefaultDateType(), getDateEnums()); }

    public GisContext getGisContext()
    { return this.gisContext; }

    public void setGisContext(GisContext gisContext)
    { this.gisContext = gisContext; }
}
