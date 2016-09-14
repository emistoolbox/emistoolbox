package com.emistoolbox.common.model.mapping;

import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import java.io.Serializable;
import java.util.List;

public abstract interface EmisDbMap extends Serializable
{
    public static final long serialVersionUID = 1L;

    public abstract EmisMeta getMetaData();

    public abstract void setMetaData(EmisMeta paramEmisMeta);

    public abstract List<DbDataSourceConfig> getDataSources();

    public abstract void setDataSources(List<DbDataSourceConfig> paramList);

    public abstract List<EmisHierarchyDbMap> getHierarchyMappings();

    public abstract void setHierarchyMappings(List<EmisHierarchyDbMap> paramList);

    public abstract List<EmisEntityDbMap> getEntityMappings();

    public abstract void setEntityMappings(List<EmisEntityDbMap> paramList);

    public abstract List<GisEntityDbMap> getGisEntityMappings();

    public abstract void setGisEntityMappings(List<GisEntityDbMap> paramList);

    public abstract GisEntityDbMap findGisMap(EmisMetaEntity paramEmisMetaEntity);

    public abstract void setGisMap(GisEntityDbMap paramGisEntityDbMap);

    public abstract List<EmisMetaDateEnum> findDateTypes(EmisMetaEntity paramEmisMetaEntity);

    public abstract List<EmisEntityDbMap> find(EmisMetaEntity paramEmisMetaEntity);

    public abstract List<EmisEntityDbMap> find(EmisMetaEntity paramEmisMetaEntity, EmisMetaDateEnum paramEmisMetaDateEnum);

    public abstract EmisHierarchyDbMap find(EmisMetaHierarchy paramEmisMetaHierarchy);

    public abstract EmisHierarchyDbMapEntry find(EmisMetaHierarchy paramEmisMetaHierarchy, EmisMetaEntity paramEmisMetaEntity1, EmisMetaEntity paramEmisMetaEntity2);
    
    public abstract void updateDimensions(); 
    
    public List<EmisDateInitDbMap> getDateInitMappings(); 

    public void setDateInitMappings(List<EmisDateInitDbMap> mappings); 
    
    public EmisDateInitDbMap findDateInit(EmisMetaDateEnum dateType);     
}
