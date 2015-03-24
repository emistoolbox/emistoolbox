package com.emistoolbox.common.model.meta;

public abstract interface EmisMetaDateEnum extends EmisMetaEnum, EmisMetaEnumTuple
{
    public abstract EmisMetaDateEnum getParent();

    public abstract void setParent(EmisMetaDateEnum paramEmisMetaDateEnum);

    public abstract boolean isAncestor(EmisMetaDateEnum paramEmisMetaDateEnum);
}
