package com.emistoolbox.common.model.impl;

import java.util.NoSuchElementException;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEntityIterator;

public abstract class EmisEntityIteratorFilter extends EmisEntityIteratorAdaptor
{
    public abstract boolean accept(EmisEntity entity);

    public EmisEntity nextValue;
    
    public EmisEntityIteratorFilter(EmisEntityIterator iter)
    { super(iter); }
    
    private void fillNext()
    {
        while (nextValue == null && super.hasNext())
        {
            EmisEntity tmp = super.next();
            if (accept(tmp))
                nextValue = tmp;
        }
    }
    
    @Override
    public boolean hasNext()
    {
        fillNext();
        return nextValue != null;
    }

    @Override
    public EmisEntity next()
    {
        fillNext();
        if (nextValue == null)
            throw new NoSuchElementException();
        
        try { return nextValue; }
        finally { nextValue = null; }
    } 
}
