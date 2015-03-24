package com.emistoolbox.common.model.analysis.impl;

import com.emistoolbox.common.model.analysis.ContextBase;
import com.emistoolbox.common.model.analysis.EmisContext;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ContextConstBooleanEntityFilter extends ContextBase implements EmisContext, Serializable
{
    private static final long serialVersionUID = 1L;
    private String name;
    private boolean value;

    public ContextConstBooleanEntityFilter() {
    }

    public ContextConstBooleanEntityFilter(String name, boolean value) {
        this.name = name;
        this.value = value;
    }

    public int getHierarchyDateIndex()
    {
        return 0;
    }

    public Boolean getBooleanEntityFilter(String key)
    {
        if (key.equals(this.name))
        {
            return Boolean.valueOf(this.value);
        }
        return null;
    }

    public Set<String> getBooleanEntityFilterNames(String entityName)
    {
        Set result = new HashSet();
        if (this.name.startsWith(entityName + "."))
        {
            result.add(this.name);
        }
        return result;
    }

    public String getName()
    {
        return this.name;
    }

    public boolean getValue()
    {
        return this.value;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.common.model.analysis.impl.ContextConstBooleanEntityFilter
 * JD-Core Version: 0.6.0
 */