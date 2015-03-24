package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.EmisHierarchyDbMap;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMapEntry;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import java.io.Serializable;
import java.util.List;

public class EmisHierarchyDbMapImpl implements EmisHierarchyDbMap, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisMetaHierarchy hierarchy;
    private List<EmisHierarchyDbMapEntry> entries;

    public EmisMetaHierarchy getHierarchy()
    { return this.hierarchy; }

    public List<EmisHierarchyDbMapEntry> getMappings()
    { return this.entries; }

    public void setHierarchy(EmisMetaHierarchy hierarchy)
    { this.hierarchy = hierarchy; }

    public void setMappings(List<EmisHierarchyDbMapEntry> entries)
    { this.entries = entries; }
    
    public void updateDimensions()
    { 
    	for (EmisHierarchyDbMapEntry entry : entries)
    		entry.updateDimensions(); 
    }
}
