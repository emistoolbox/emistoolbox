package com.emistoolbox.common.results;

import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;

public abstract interface DateEnumAwareDimension
{
    public abstract EmisEnumTupleValue getDateEnum();

    public abstract void setDateEnum(EmisEnumTupleValue paramEmisEnumTupleValue);

//    public abstract boolean supportsDateEnum(EmisMetaDateEnum paramEmisMetaDateEnum);
}
