package com.emistoolbox.common.model.analysis.impl;

import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.ContextBase;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ContextConstDate extends ContextBase implements EmisContext, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisEnumTupleValue value;

    public ContextConstDate() 
    {}

    public ContextConstDate(EmisEnumTupleValue value) 
    { this.value = value; }

    public EmisMetaDateEnum getDateType()
    { return (EmisMetaDateEnum) this.value.getEnumTuple(); }

    public List<EmisEnumTupleValue> getDates()
    {
        List result = new ArrayList();
        result.add(this.value);
        return result;
    }

    public void setDates(Collection<EmisEnumTupleValue> dates)
    {
        if (dates.size() > 0)
        {
            Iterator i$ = dates.iterator();
            if (i$.hasNext())
            {
                EmisEnumTupleValue date = (EmisEnumTupleValue) i$.next();

                this.value = date;
                return;
            }
        }
        else
            this.value = null;
    }

    public void setDate(EmisEnumTupleValue date)
    { this.value = date; }

	@Override
	public int getHierarchyDateIndex() 
	{ return value.getIndex()[0]; }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.model.analysis.impl.ContextConstDate
 * JD-Core Version: 0.6.0
 */