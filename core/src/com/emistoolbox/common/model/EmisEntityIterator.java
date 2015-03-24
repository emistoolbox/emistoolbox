package com.emistoolbox.common.model;

import java.util.Iterator;

import com.emistoolbox.common.model.meta.EmisMetaEntity;

public interface EmisEntityIterator extends Iterator<EmisEntity>
{
    /** @return Flag whether EmisEntities contain the name of the entity. */
    public boolean hasNames();
    
    /** @return Entity type of the iterated entities. */
    public EmisMetaEntity getEntityType(); 
}
