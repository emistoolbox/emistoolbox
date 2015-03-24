package com.emistoolbox.common.model.mapping;

import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import java.util.List;

public abstract interface EmisHierarchyDbMap
{
    public abstract EmisMetaHierarchy getHierarchy();

    public abstract void setHierarchy(EmisMetaHierarchy paramEmisMetaHierarchy);

    public abstract List<EmisHierarchyDbMapEntry> getMappings();

    public abstract void setMappings(List<EmisHierarchyDbMapEntry> paramList);
    
    public abstract void updateDimensions(); 
}
