package com.emistoolbox.server.results.impl;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.impl.ContextConstEntity;
import com.emistoolbox.server.results.ResultDimension;
import java.util.ArrayList;
import java.util.List;

public class ResultDimensionEntity implements ResultDimension
{
    private EmisEntity[] entities = null;
    private int dateIndex;

    public ResultDimensionEntity(EmisEntity[] entities, int dateIndex) {
        this.entities = entities;
        this.dateIndex = dateIndex;
    }

    public EmisContext getContext(int index)
    {
        return new ContextConstEntity(this.entities[index], this.dateIndex);
    }

    public int getItemCount()
    {
        return this.entities.length;
    }

    public String getItemName(int index)
    {
        return this.entities[index].getName();
    }

    public void updateContext(int index, EmisContext context)
    {
        context.setEntityType(this.entities[index].getEntityType());
        context.setHierarchyDateIndex(this.dateIndex);
        List result = new ArrayList();
        result.add(this.entities[index]);
        context.setEntities(result);
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.results.impl.ResultDimensionEntity
 * JD-Core Version: 0.6.0
 */