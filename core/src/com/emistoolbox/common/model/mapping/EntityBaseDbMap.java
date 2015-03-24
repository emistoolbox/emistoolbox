package com.emistoolbox.common.model.mapping;

import com.emistoolbox.common.model.meta.EmisMetaEntity;

public abstract interface EntityBaseDbMap extends EmisDbMapBase
{
    public abstract EmisMetaEntity getEntity();

    public abstract void setEmisMetaEntity(EmisMetaEntity paramEmisMetaEntity);

    public abstract DbRowAccess getIdAccess();

    public abstract void setIdAccess(DbRowAccess paramDbRowAccess);
}
