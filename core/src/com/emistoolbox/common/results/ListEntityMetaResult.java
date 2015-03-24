package com.emistoolbox.common.results;

import com.emistoolbox.common.model.meta.EmisMetaEntity;

public interface ListEntityMetaResult extends MetaResult 
{
    public EmisMetaEntity getListEntity();
    public void setListEntity(EmisMetaEntity paramEmisMetaEntity);
}
