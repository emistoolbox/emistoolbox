package com.emistoolbox.common.model.analysis.impl;

import com.emistoolbox.common.model.analysis.ContextBase;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.util.NamedUtil;
import java.io.Serializable;
import java.util.Set;

public class ContextConstEntityFilter extends ContextBase implements EmisContext, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisMetaData field;
    private int index;

    public ContextConstEntityFilter() {
    }

    public ContextConstEntityFilter(EmisMetaData field, int index) {
        this.field = field;
        this.index = index;
    }

    public boolean allowEntityWithValue(EmisMetaData field, int value)
    {
        if (this.field.equals(field))
        {
            return value == this.index;
        }
        return true;
    }

    public Set<String> getEntityFilterNames(EmisMetaEntity entity)
    {
        Set result = super.getEntityFilterNames(entity);
        if (NamedUtil.sameName(entity, this.field.getEntity()))
        {
            result.add(this.field.getName());
        }
        return result;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.common.model.analysis.impl.ContextConstEntityFilter JD-Core
 * Version: 0.6.0
 */