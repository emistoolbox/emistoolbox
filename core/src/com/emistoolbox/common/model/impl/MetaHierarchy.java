package com.emistoolbox.common.model.impl;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.impl.NamedImpl;
import java.io.Serializable;

public class MetaHierarchy extends NamedImpl implements EmisMetaHierarchy, Serializable
{
    private static final long serialVersionUID = 1L;
    NamedIndexList<EmisMetaEntity> entities;

    public NamedIndexList<EmisMetaEntity> getEntityOrder()
    {
        return this.entities;
    }

    public void setEntityOrder(NamedIndexList<EmisMetaEntity> entities)
    {
        this.entities = entities;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.model.impl.MetaHierarchy JD-Core
 * Version: 0.6.0
 */