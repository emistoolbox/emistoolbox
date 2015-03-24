package com.emistoolbox.common.results;

import com.emistoolbox.common.model.meta.EmisMetaEntity;

public abstract interface EntityAwareDimension
{
    public abstract EmisMetaEntity getEntityType();

    public abstract void setEntityType(EmisMetaEntity paramEmisMetaEntity);
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.results.EntityAwareDimension JD-Core
 * Version: 0.6.0
 */