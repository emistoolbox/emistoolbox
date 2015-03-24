package com.emistoolbox.common.results.impl;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.results.EntityAwareDimension;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.common.util.impl.NamedImpl;

public abstract class MetaResultDimensionEntity extends NamedImpl implements MetaResultDimension, EntityAwareDimension
{
    private static final long serialVersionUID = 1L;
    private EmisMetaHierarchy hierarchy;
    private EmisMetaEntity entityType;
    private int dateIndex;
    private int[] entityIds;
    private String[] entityNames;

    public EmisMetaHierarchy getHierarchy()
    {
        return this.hierarchy;
    }

    public void setHierarchy(EmisMetaHierarchy hierarchy)
    { this.hierarchy = hierarchy; }

    public int getHierarchyDateIndex()
    { return this.dateIndex; }

    public void setDateIndex(int dateIndex)
    { this.dateIndex = dateIndex; }

    public EmisMetaEntity getEntityType()
    { return this.entityType; }

    public void setEntityType(EmisMetaEntity entityType)
    { this.entityType = entityType; }

    public int[] getEntityPath()
    { return this.entityIds; }

    public String[] getEntityPathNames()
    { return this.entityNames; }

    public void setPath(int[] entityIds, String[] entityNames, int dateIndex)
    {
        this.dateIndex = dateIndex;

        if (this.entityType != null)
        {
            int entityIndex = NamedUtil.findIndex(this.entityType, this.hierarchy.getEntityOrder());
            if (entityIds.length != entityIndex + 1)
                throw new IllegalArgumentException("Expected " + (entityIndex + 1) + " ids, not " + entityIds.length);
            if (entityNames.length != entityIndex + 1)
            {
                throw new IllegalArgumentException("Expected " + (entityIndex + 1) + " names, not " + entityNames.length);
            }
        }
        else if ((entityIds.length != 0) || (entityNames.length != 0))
        {
            throw new IllegalArgumentException("Expected 0 length ids for top level.");
        }
        this.entityIds = entityIds;
        this.entityNames = entityNames;
    }

    public String getName()
    {
        String result = super.getName();
        if (this.entityType == null)
            return result;

        if ((this.entityIds != null) && (this.entityNames[(this.entityIds.length - 1)] != null))
            result = result + " '" + this.entityNames[(this.entityIds.length - 1)] + "'";

        return result;
    }
}

