package com.emistoolbox.common.results.impl;

import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.results.DateEnumAwareDimension;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.util.impl.NamedImpl;

public class MetaResultDimensionDate extends NamedImpl implements MetaResultDimension, DateEnumAwareDimension
{
    private static final long serialVersionUID = 1L;
  
    private EmisMetaDateEnum dateType;
    private EmisEnumTupleValue value;

    public EmisEnumTupleValue getDateEnum()
    { return this.value; }

    public void setDateEnum(EmisEnumTupleValue date)
    { this.value = date; }

//    public boolean supportsDateEnum(EmisMetaDateEnum dateEnum)
//    { return dateEnum == this.dateType; }

    public EmisMetaDateEnum getDateEnumType()
    { return this.dateType; }

    public void setDateEnumType(EmisMetaDateEnum dateType)
    { this.dateType = dateType; }

    public String getName()
    {
        String result = normalize(super.getName());
        if (this.value != null)
        {
            result = result + " '" + this.value.toString() + "'";
        }
        return result;
    }
}
