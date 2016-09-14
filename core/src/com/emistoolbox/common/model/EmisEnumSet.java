package com.emistoolbox.common.model;

import com.emistoolbox.common.model.meta.EmisMetaEnum;
import java.io.Serializable;
import java.util.Set;

public interface EmisEnumSet extends Serializable
{
    public EmisMetaEnum getEnum();

    public void setEnum(EmisMetaEnum paramEmisMetaEnum);

    public void clear();

    public void setAll();

    public void addValue(String paramString);

    public void addValue(byte paramByte);

    public void removeValue(String paramString);

    public void removeValue(byte paramByte);

    public boolean hasValue(String paramString);

    public boolean hasValue(byte paramByte);

    public boolean hasAllValues();

    public Set<String> getAll();

    public void setAll(Set<String> paramSet);

    public Set<Byte> getAllIndexes();

    public void setAllIndexes(Set<Byte> paramSet);

    public int getTotalCount();

    public int getSetCount();
    
    public void opAnd(EmisEnumSet values); 
    
    public void opOr(EmisEnumSet values); 
    
    public void opNot(); 
    
    public EmisEnumSet createCopy(); 
}
