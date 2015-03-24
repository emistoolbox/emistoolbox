package com.emistoolbox.common.model.meta;

import com.emistoolbox.common.util.Named;

public abstract interface EmisMetaEnum extends Named
{
    public abstract String[] getValues();

    public abstract void setValues(String[] paramArrayOfString);

    public abstract String getValue(byte paramByte);
    
    public abstract String getSetValues(int bits); 
    
    public abstract int getSetIndexes(String values); 

    public abstract byte getIndex(String paramString);

    public abstract byte getSize();
}
