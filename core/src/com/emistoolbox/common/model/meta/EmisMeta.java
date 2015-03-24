package com.emistoolbox.common.model.meta;

import com.emistoolbox.common.util.NamedIndexList;

public abstract interface EmisMeta
{
    public String getDatasetName(); 
    public void setDatasetName(String dataset); 
    
    public abstract NamedIndexList<EmisMetaEntity> getEntities();

    public abstract void setEntities(NamedIndexList<EmisMetaEntity> paramNamedIndexList);

    public abstract NamedIndexList<EmisMetaHierarchy> getHierarchies();

    public abstract void setHierarchies(NamedIndexList<EmisMetaHierarchy> paramNamedIndexList);

    public abstract NamedIndexList<EmisMetaEnum> getEnums();

    public abstract void setEnums(NamedIndexList<EmisMetaEnum> paramNamedIndexList);

    public abstract NamedIndexList<EmisMetaDateEnum> getDateEnums();

    public abstract void setDateEnums(NamedIndexList<EmisMetaDateEnum> paramNamedIndexList);

    public abstract EmisMetaDateEnum getDefaultDateType();

    public abstract int getDefaultDateTypeIndex();

    public abstract GisContext getGisContext(); 

    public abstract void setGisContext(GisContext context); 
}
