package com.emistoolbox.common.model.meta;

import com.emistoolbox.common.util.Named;
import com.emistoolbox.common.util.NamedIndexList;

public abstract interface EmisMetaHierarchy extends Named
{
    public abstract NamedIndexList<EmisMetaEntity> getEntityOrder();
    public abstract void setEntityOrder(NamedIndexList<EmisMetaEntity> entities);
}
