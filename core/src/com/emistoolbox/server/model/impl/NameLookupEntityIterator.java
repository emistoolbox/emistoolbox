package com.emistoolbox.server.model.impl;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEntityIterator;
import com.emistoolbox.common.model.impl.EmisEntityIteratorAdaptor;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityData;
import com.emistoolbox.server.model.EmisEntityDataSet;

public class NameLookupEntityIterator extends EmisEntityIteratorAdaptor
{
    private EmisEntityDataSet entityDataset; 
    private int dateIndex;
    private EntityDataAccess valueAccess;

    public NameLookupEntityIterator(EmisDataSet dataset, int entityTypeIndex, int dateIndex, EmisEntityIterator iter)
    {
        super(iter);
        if (iter.hasNames())
            throw new IllegalArgumentException("EmisEntityIterator should not provide names.");
        
        this.entityDataset = dataset.getEntityDataSet(entityTypeIndex, dateIndex);
        this.dateIndex = dateIndex;
        
        valueAccess = entityDataset.getDataAccess("name");
    }
    
    @Override
    public EmisEntity next()
    { 
        EmisEntity result = super.next();
        result.setName(getName(result.getId()));
        return result;
    }

    @Override
    public boolean hasNames()
    { return true; }

    private String getName(int entityId)
    {
        EmisEntityData entityData = entityDataset.getData(dateIndex, entityId);
        return valueAccess.getAsString(entityData.getMasterArray());
    }
}
