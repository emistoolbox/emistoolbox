package com.emistoolbox.common.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEntityIterator;
import com.emistoolbox.common.model.meta.EmisMetaEntity;

public class IntArrayEntityIterator implements EmisEntityIterator
{
    private EmisMetaEntity entityType = null;
    private List<int[]> ids = null;
    
    private Map<Integer, String> names = null;

    private Integer nextValue = null;
    private int idsIndex = 0;
    private int valueIndex = 0;
    
    public IntArrayEntityIterator(EmisMetaEntity entityType, int[] ids)
    {
        this.entityType = entityType;
        this.ids = new ArrayList<int[]>();
        if (ids != null)
            this.ids.add(ids);
    }
    
    public IntArrayEntityIterator(EmisMetaEntity entityType, List<int[]> ids)
    {
        this.entityType = entityType;
        this.ids = new ArrayList<int[]>();
        for (int[] tmpIds : ids)
        {
            if (tmpIds != null)
                this.ids.add(tmpIds);
        }
    }
    
    public List<int[]> getIds()
    { return ids; }
    
    public void setNames(Map<Integer, String> names)
    { this.names = names; }
    
    @Override
    public boolean hasNext()
    { 
        findNextId();
        return nextValue != null;
    }

    private synchronized void findNextId()
    {
        if (nextValue != null)
            return;
        
        if (idsIndex >= ids.size())
            return; 
        
        int[] tmp = this.ids.get(idsIndex);
        if (valueIndex >= tmp.length)
        {
            valueIndex = 0;
            idsIndex++;
            findNextId();
            return;
        }

        nextValue = tmp[valueIndex];
        valueIndex++;
        if (nextValue == -1)
        {
            nextValue = null;
            findNextId();
            return;
        }
    }
    
    @Override
    public synchronized EmisEntity next()
    {
        findNextId();
        if (nextValue == null)
            throw new NoSuchElementException();

        EmisEntity result = new Entity(entityType, nextValue);
        if (hasNames())
            result.setName(names.get(nextValue));
        nextValue = null;

        return result;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException(); }

    @Override
    public boolean hasNames()
    { return names != null; }

    @Override
    public EmisMetaEntity getEntityType()
    { return entityType; }
}
