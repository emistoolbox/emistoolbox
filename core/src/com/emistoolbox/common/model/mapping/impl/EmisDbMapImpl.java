package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import com.emistoolbox.common.model.mapping.EmisDbMap;
import com.emistoolbox.common.model.mapping.EmisEntityDbMap;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMap;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMapEntry;
import com.emistoolbox.common.model.mapping.GisEntityDbMap;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.NamedUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EmisDbMapImpl implements EmisDbMap, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisMeta meta;
    private List<DbDataSourceConfig> dataSources = new ArrayList();
    private List<EmisHierarchyDbMap> hierarchyMaps = new ArrayList();
    private List<EmisEntityDbMap> entityMaps = new ArrayList();
    private List<GisEntityDbMap> gisMaps = new ArrayList();

    public EmisMeta getMeta()
    {
        return this.meta;
    }

    public void setMeta(EmisMeta meta)
    {
        this.meta = meta;
    }

    public List<EmisEntityDbMap> getEntityMappings()
    {
        return this.entityMaps;
    }

    public List<EmisHierarchyDbMap> getHierarchyMappings()
    {
        return this.hierarchyMaps;
    }

    public void setEntityMappings(List<EmisEntityDbMap> mappings)
    {
        this.entityMaps = mappings;
    }

    public void setHierarchyMappings(List<EmisHierarchyDbMap> mappings)
    { this.hierarchyMaps = mappings; }

    public List<DbDataSourceConfig> getDataSources()
    { return this.dataSources; }

    public void setDataSources(List<DbDataSourceConfig> sources)
    { this.dataSources = sources; }

    public List<EmisEntityDbMap> find(EmisMetaEntity entity)
    {
        List result = new ArrayList();
        for (EmisEntityDbMap map : this.entityMaps)
        {
            if (NamedUtil.sameName(map.getEntity(), entity))
                result.add(map);
        }

        return result;
    }

    public List<EmisEntityDbMap> find(EmisMetaEntity entity, EmisMetaDateEnum dateType)
    {
        List result = new ArrayList();
        for (EmisEntityDbMap map : this.entityMaps)
        {
            if ((NamedUtil.sameName(map.getEntity(), entity)) && (NamedUtil.sameName(map.getDateEnum(), dateType)))
                result.add(map);
        }

        return result;
    }

    public List<EmisMetaDateEnum> findDateTypes(EmisMetaEntity entity)
    {
        List result = new ArrayList();
        for (EmisMetaData data : entity.getData())
        {
            EmisMetaDateEnum tmp = data.getDateType();
            if ((tmp != null) && (!result.contains(tmp)))
                result.add(tmp);
        }

        return result;
    }

    public EmisHierarchyDbMapEntry find(EmisMetaHierarchy hierarchy, EmisMetaEntity parent, EmisMetaEntity child)
    {
        EmisHierarchyDbMap map = find(hierarchy);
        if (map == null)
            return null;

        for (EmisHierarchyDbMapEntry entry : map.getMappings())
        {
            if ((NamedUtil.sameName(entry.getChildEntity(), child)) && (NamedUtil.sameName(entry.getParentEntity(), parent)))
                return entry;
        }

        return null;
    }

    public EmisHierarchyDbMap find(EmisMetaHierarchy hierarchy)
    {
        for (EmisHierarchyDbMap map : this.hierarchyMaps)
        {
            if (NamedUtil.sameName(map.getHierarchy(), hierarchy))
                return map;
        }
        
        return null;
    }

    public EmisMeta getMetaData()
    { return this.meta; }

    public void setMetaData(EmisMeta meta)
    { this.meta = meta; }

    public GisEntityDbMap findGisMap(EmisMetaEntity entity)
    {
        for (GisEntityDbMap map : this.gisMaps)
        {
            if (NamedUtil.sameName(map.getEntity(), entity))
                return map;
        }

        return null;
    }

    public List<GisEntityDbMap> getGisEntityMappings()
    { return this.gisMaps; }

    public void setGisEntityMappings(List<GisEntityDbMap> mappings)
    { this.gisMaps = mappings; }

    public void setGisMap(GisEntityDbMap newMap)
    {
        GisEntityDbMap oldMap = findGisMap(newMap.getEntity());
        if (oldMap != null)
        {
            this.gisMaps.remove(oldMap);
        }
        this.gisMaps.add(newMap);
    }
    
    public void updateDimensions()
    {
        for (EmisHierarchyDbMap map : hierarchyMaps)
        	map.updateDimensions(); 
        
        for (EmisEntityDbMap map : entityMaps)
        	map.updateDimensions(); 

        for (GisEntityDbMap map : gisMaps)
        	map.updateDimensions(); 
    }
}
