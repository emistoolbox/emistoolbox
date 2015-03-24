package com.emistoolbox.common.model.impl;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEntityIterator;
import com.emistoolbox.common.model.meta.EmisMetaEntity;

public class EmisEntityIteratorAdaptor implements EmisEntityIterator
{
    private EmisEntityIterator iter;
    
    public EmisEntityIteratorAdaptor(EmisEntityIterator iter)
    { this.iter = iter; }

    @Override
    public boolean hasNext()
    { return iter.hasNext(); }

    @Override
    public EmisEntity next()
    { return iter.next(); }

    @Override
    public void remove()
    { iter.remove(); }

    @Override
    public boolean hasNames()
    { return iter.hasNames(); }

    @Override
    public EmisMetaEntity getEntityType()
    { return iter.getEntityType(); }
}
