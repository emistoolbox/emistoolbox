package com.emistoolbox.common.model;

import com.emistoolbox.common.model.meta.EmisMetaEnum;
import java.io.Serializable;
import java.util.Set;

public abstract interface EmisEnumSet extends Serializable
{
    public abstract EmisMetaEnum getEnum();

    public abstract void setEnum(EmisMetaEnum paramEmisMetaEnum);

    public abstract void clear();

    public abstract void setAll();

    public abstract void addValue(String paramString);

    public abstract void addValue(byte paramByte);

    public abstract void removeValue(String paramString);

    public abstract void removeValue(byte paramByte);

    public abstract boolean hasValue(String paramString);

    public abstract boolean hasValue(byte paramByte);

    public abstract boolean hasAllValues();

    public abstract Set<String> getAll();

    public abstract void setAll(Set<String> paramSet);

    public abstract Set<Byte> getAllIndexes();

    public abstract void setAllIndexes(Set<Byte> paramSet);

    public abstract int size();
}
