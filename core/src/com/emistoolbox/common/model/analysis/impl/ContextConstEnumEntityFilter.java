package com.emistoolbox.common.model.analysis.impl;

import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.analysis.ContextBase;
import com.emistoolbox.common.model.analysis.EmisContext;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ContextConstEnumEntityFilter extends ContextBase implements EmisContext, Serializable
{
    private static final long serialVersionUID = 1L;
    private String name;
    private EmisEnumSet values;

    public ContextConstEnumEntityFilter() {
    }

    public ContextConstEnumEntityFilter(String name, EmisEnumSet values) {
        this.name = name;
        this.values = values;
    }

    public int getHierarchyDateIndex()
    {
        return 0;
    }

    public EmisEnumSet getEnumEntityFilter(String key)
    {
        if (this.name.equals(key))
            return this.values;
        return null;
    }

    public Set<String> getEnumEntityFilterNames(String entityName)
    {
        Set result = new HashSet();
        if (this.name.startsWith(entityName + "."))
        {
            result.add(this.name);
        }
        return result;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.common.model.analysis.impl.ContextConstEnumEntityFilter
 * JD-Core Version: 0.6.0
 */