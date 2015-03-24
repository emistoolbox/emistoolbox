package com.emistoolbox.client.ui.results;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.ui.Widget;

public abstract interface IHierarchyBrowser extends HasValueChangeHandlers<EmisEntity>
{
    public abstract void setEmisMeta(EmisMeta paramEmisMeta, EmisMetaHierarchy paramEmisMetaHierarchy);

    public abstract void setDateIndex(int paramInt);

    public abstract void setAnySelection(boolean paramBoolean);

    public abstract boolean hasAnySelection();

    public abstract EmisMetaEntity getEntityType();

    public abstract void setEntityType(EmisMetaEntity paramEmisMetaEntity);

    public abstract int getEntityTypeIndex();

    public abstract void setPath(int[] paramArrayOfInt, String[] paramArrayOfString);

    public abstract void setPath(int paramInt1, int paramInt2, String paramString);

    public abstract void setPathId(int paramInt1, int paramInt2);

    public abstract int getPathId(int paramInt);

    public abstract int[] getPathIds();

    public abstract void setPathName(int paramInt, String paramString);

    public abstract String getPathName(int paramInt);

    public abstract String[] getPathNames();

    public abstract EmisMetaHierarchy getHierarchy();

    public abstract void setHierarchy(EmisMetaHierarchy paramEmisMetaHierarchy);

    public abstract int getDateIndex();

    public abstract Widget getWidget();
}

