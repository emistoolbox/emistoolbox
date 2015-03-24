package com.emistoolbox.common.model.mapping;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;

public abstract interface EmisHierarchyDbMapEntry extends EmisDbMapBase
{
    public abstract EmisMetaEntity getParentEntity();

    public abstract void setParentEntity(EmisMetaEntity paramEmisMetaEntity);

    public abstract EmisMetaDateEnum getDateType();

    public abstract void setDateType(EmisMetaDateEnum paramEmisMetaDateEnum);

    public abstract DbRowDateAccess getDateAccess();

    public abstract void setDateAccess(DbRowDateAccess paramDbRowDateAccess);

    public abstract DbRowAccess getParentAccess();

    public abstract void setParentAccess(DbRowAccess paramDbRowAccess);

    public abstract EmisMetaEntity getChildEntity();

    public abstract void setChildEntity(EmisMetaEntity paramEmisMetaEntity);

    public abstract DbRowAccess getChildAccess();

    public abstract void setChildAccess(DbRowAccess paramDbRowAccess);
}
