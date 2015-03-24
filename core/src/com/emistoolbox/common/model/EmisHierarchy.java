package com.emistoolbox.common.model;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import java.util.List;

public abstract interface EmisHierarchy
{
    public static final int DEFAULT_DATE = -1;

    public abstract EmisMetaEnum getDateType();

    public abstract EmisMetaHierarchy getMetaHierarchy();

    public abstract List<int[]> getDescendants(int paramInt1, EmisMetaEntity paramEmisMetaEntity1, int paramInt2, EmisMetaEntity paramEmisMetaEntity2);

    public abstract int[] getRootElements(int paramInt);

    public abstract int[] getChildren(int dateIndex, EmisMetaEntity parentEntityType, int parentId);

    public abstract EmisEntityIterator getChildren(int dateIndex, EmisEntity parentEntity);
    
    public abstract Integer findParentId(EmisEntity child, int dateIndex); 
    
    public void setChildren(int dateIndex, int hierarchyIndex, int id, int[] children); 
    
    public void setChildren(int dateIndex, EmisMetaEntity parentType, int parentId, int[] children); 
}
