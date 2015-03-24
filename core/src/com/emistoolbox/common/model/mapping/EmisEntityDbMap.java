package com.emistoolbox.common.model.mapping;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import java.util.Map;

public abstract interface EmisEntityDbMap extends EntityBaseDbMap, EmisDbMapBase
{
    public abstract Map<String, DbRowAccess> getFieldAccess();

    public abstract void setFieldAccess(Map<String, DbRowAccess> paramMap);

    public abstract EmisMetaDateEnum getDateEnum();

    public abstract void setDateEnum(EmisMetaDateEnum paramEmisMetaDateEnum);

    public abstract DbRowDateAccess getDateAccess();

    public abstract void setDateAccess(DbRowDateAccess paramDbRowDateAccess);
}
