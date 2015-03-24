package com.emistoolbox.common.model;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.util.Named;

public abstract interface EmisEntity extends Named
{
    public abstract int getId();

    public abstract void setId(int paramInt);

    public abstract EmisMetaEntity getEntityType();

    public abstract void setEntityType(EmisMetaEntity paramEmisMetaEntity);
}
