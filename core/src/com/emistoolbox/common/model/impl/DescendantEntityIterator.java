package com.emistoolbox.common.model.impl;

import java.util.List;
import java.util.NoSuchElementException;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEntityIterator;
import com.emistoolbox.common.model.EmisHierarchy;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.util.NamedUtil;

public class DescendantEntityIterator implements EmisEntityIterator
{
    private EmisHierarchy hierarchy;
    private int dateIndex;

    private EmisEntityIterator parents;
    private EmisMetaEntity targetEntityType;

    private EmisEntityIterator currentTargetIterator;
    
    public DescendantEntityIterator(EmisHierarchy hierarchy, int dateIndex, EmisEntity parent, EmisMetaEntity targetEntityType)
    { this(hierarchy, dateIndex, new IntArrayEntityIterator(parent.getEntityType(), new int[] { parent.getId() }), targetEntityType); }

    public DescendantEntityIterator(EmisHierarchy hierarchy, int dateIndex, List<EmisEntity> parents, EmisMetaEntity targetEntityType)
    { this(hierarchy, dateIndex, new IntArrayEntityIterator(getEntityType(parents), getEntityIds(parents)), targetEntityType); }

    private static EmisMetaEntity getEntityType(List<EmisEntity> items)
    {
        if (items.size() == 0)
            throw new IllegalArgumentException("No entities specified.");

        return items.get(0).getEntityType();
    }
    
    private static int[] getEntityIds(List<EmisEntity> items)
    {
        int[] result = new int[items.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = items.get(i).getId();
        
        return result;
    }
    
    public DescendantEntityIterator(EmisHierarchy hierarchy, int dateIndex, EmisEntityIterator parents, EmisMetaEntity targetEntityType)
    {
        this.hierarchy = hierarchy;
        this.dateIndex = dateIndex;
        this.parents = parents;
        this.targetEntityType = targetEntityType;
        
        prepareNext();
    }

    private void prepareNext()
    {
        if (currentTargetIterator != null && currentTargetIterator.hasNext())
            return;
        
        // Need new iterator.
        //
        if (parents.hasNext())
        {
            EmisEntityIterator iter = hierarchy.getChildren(dateIndex, parents.next()); 
            if (iter != null)
            {
                if (NamedUtil.sameName(iter.getEntityType(), targetEntityType))
                    currentTargetIterator = iter;
                else
                    currentTargetIterator = new DescendantEntityIterator(hierarchy, dateIndex, iter, targetEntityType);
            }
            else
                currentTargetIterator = null;
        }
        else
            currentTargetIterator = null;
    }
    
    @Override
    public boolean hasNext()
    {
        prepareNext();
        return currentTargetIterator != null && currentTargetIterator.hasNext();
    }

    @Override
    public EmisEntity next()
    {
        prepareNext();
        if (currentTargetIterator == null)
            throw new NoSuchElementException();
        
        return currentTargetIterator.next();
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException(); }

    @Override
    public boolean hasNames()
    { return false; }

    @Override
    public EmisMetaEntity getEntityType()
    { return targetEntityType; }
}
