package com.emistoolbox.common.model;

import java.util.Set;

import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;

public abstract interface EmisEnumTupleValue
{
    public EmisMetaEnumTuple getEnumTuple();

    public void setEnumTuple(EmisMetaEnumTuple paramEmisMetaEnumTuple);

    public String[] getValue();

    public void setValue(String[] paramArrayOfString);

    public byte[] getIndex();

    public void setIndex(byte[] paramArrayOfByte);

    public <T extends EmisMetaEnumTuple> EmisEnumTupleValue get(T dateType);

    public <T extends EmisMetaEnumTuple> EmisEnumTupleValue get(Set<T> dateTypes);
    
    public EmisEnumTupleValue createCopy(); 
}
