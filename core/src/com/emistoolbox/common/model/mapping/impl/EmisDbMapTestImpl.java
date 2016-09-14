package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import com.emistoolbox.common.model.mapping.EmisDateInitDbMap;
import com.emistoolbox.common.model.mapping.EmisDbMap;
import com.emistoolbox.common.model.mapping.EmisEntityDbMap;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMap;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMapEntry;
import com.emistoolbox.common.model.mapping.GisEntityDbMap;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EmisDbMapTestImpl implements EmisDbMap, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisDbMap mapping;
    private List<EmisEntityDbMap> entityMapping;
    private boolean withGis = true;
    private boolean withHierarchy = true;

    public EmisDbMapTestImpl(EmisDbMap mapping, EmisEntityDbMap entityMap) 
    {
        this.mapping = mapping;
        this.entityMapping = new ArrayList<EmisEntityDbMap>();
        this.entityMapping.add(entityMap);
    }

    public void setWithGis(boolean withGis)
    { this.withGis = withGis; }

    public void setWithHiearchy(boolean withHiearchy)
    { this.withHierarchy = withHiearchy; }

    public List<EmisEntityDbMap> find(EmisMetaEntity entity)
    { return this.mapping.find(entity); }

    public List<EmisEntityDbMap> find(EmisMetaEntity entity, EmisMetaDateEnum dateType)
    { return this.mapping.find(entity, dateType); }

    public EmisHierarchyDbMap find(EmisMetaHierarchy hierarchy)
    { return this.withHierarchy ? this.mapping.find(hierarchy) : null; }

    public EmisHierarchyDbMapEntry find(EmisMetaHierarchy hierarchy, EmisMetaEntity parent, EmisMetaEntity child)
    { return this.mapping.find(hierarchy, parent, child); }

    public List<EmisMetaDateEnum> findDateTypes(EmisMetaEntity entity)
    { return this.mapping.findDateTypes(entity); }

    public GisEntityDbMap findGisMap(EmisMetaEntity entity)
    { return this.withGis ? this.mapping.findGisMap(entity) : null; }

    public List<DbDataSourceConfig> getDataSources()
    { return this.mapping.getDataSources(); }

    public List<EmisEntityDbMap> getEntityMappings()
    { return this.entityMapping; }

    public List<GisEntityDbMap> getGisEntityMappings()
    { return this.withGis ? this.mapping.getGisEntityMappings() : new ArrayList(); }

    public List<EmisHierarchyDbMap> getHierarchyMappings()
    { return this.withHierarchy ? this.mapping.getHierarchyMappings() : new ArrayList(); }

    public EmisMeta getMetaData()
    { return this.mapping.getMetaData(); }

    public void setDataSources(List<DbDataSourceConfig> sources)
    { this.mapping.setDataSources(sources); }

    public void setEntityMappings(List<EmisEntityDbMap> mappings)
    { this.mapping.setEntityMappings(mappings); }

    public void setGisEntityMappings(List<GisEntityDbMap> mappings)
    { this.mapping.setGisEntityMappings(mappings); }

    public void setGisMap(GisEntityDbMap map)
    { this.mapping.setGisMap(map); }

    public void setHierarchyMappings(List<EmisHierarchyDbMap> mappings)
    { this.mapping.setHierarchyMappings(mappings); }

    public void setMetaData(EmisMeta meta)
    { this.mapping.setMetaData(meta); }

	public void updateDimensions() 
	{ this.mapping.updateDimensions(); }

	@Override
	public List<EmisDateInitDbMap> getDateInitMappings() 
	{ return mapping.getDateInitMappings(); } 

	@Override
	public void setDateInitMappings(List<EmisDateInitDbMap> mappings) 
	{ mapping.setDateInitMappings(mappings); }

	@Override
	public EmisDateInitDbMap findDateInit(EmisMetaDateEnum dateType) 
	{ return mapping.findDateInit(dateType); } 
}
