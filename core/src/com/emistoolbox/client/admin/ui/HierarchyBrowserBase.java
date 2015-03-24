package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.StatusAsyncCallback;
import com.emistoolbox.client.ui.results.IHierarchyBrowser;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

import java.util.List;
import java.util.Map;

public abstract class HierarchyBrowserBase implements IHierarchyBrowser
{
    protected EmisToolbox emis;
    protected EmisMeta meta;
    private int[] pathIds;
    private String[] pathNames;
    private EmisMetaEntity entityType = null;
    private int entityTypeIndex = -1;
    private boolean anySelection = false;

    private int dateIndex = 0;
    private EmisMetaHierarchy hierarchy;
    private HandlerManager manager = new HandlerManager(getWidget());

    private List<EmisEntity> rootEntities; 

    public HierarchyBrowserBase(EmisToolbox emis, List<EmisEntity> rootEntities) 
    {
        this.emis = emis;
        this.rootEntities = rootEntities; 
    }
    
    public List<EmisEntity> getRootEntities()
    { return rootEntities; }
    
    public void setRootEntities(List<EmisEntity> entities)
    { this.rootEntities = entities; } 
    
    public EmisMetaHierarchy getHierarchy()
    {
        return this.hierarchy;
    }

    public void setHierarchy(EmisMetaHierarchy hierarchy)
    {
        this.hierarchy = hierarchy;

        int size = hierarchy.getEntityOrder().size();
        if (this.entityType != null)
        {
            this.entityTypeIndex = NamedUtil.findIndex(this.entityType, hierarchy.getEntityOrder());
            size = this.entityTypeIndex + 1;
        }

        this.pathIds = new int[size];
        this.pathNames = new String[size];

        updateUi();
    }

    public int getDateIndex()
    {
        return this.dateIndex;
    }

    public int[] getPathIds()
    {
        return this.pathIds;
    }

    public String[] getPathNames()
    {
        return this.pathNames;
    }

    public boolean hasAnySelection()
    {
        return this.anySelection;
    }

    public void setAnySelection(boolean anySelection)
    {
        this.anySelection = anySelection;
    }

    public void setDateIndex(int dateIndex, boolean allowChange)
    {
        setDateIndex(dateIndex);
    }

    public void setDateIndex(int dateIndex)
    {
        this.dateIndex = dateIndex;
    }

    public void setEmisMeta(EmisMeta meta, EmisMetaHierarchy newHierarchy)
    {
        this.meta = meta;
        if (this.entityType != null)
        {
            this.entityTypeIndex = NamedUtil.findIndex(this.entityType.getName(), newHierarchy.getEntityOrder());
        }
        setHierarchy(newHierarchy);
    }

    public EmisMetaEntity getEntityType()
    {
        return this.entityType;
    }

    public int getEntityTypeIndex()
    {
        return this.entityTypeIndex;
    }

    public void setEntityType(EmisMetaEntity entityType)
    {
        this.entityType = entityType;
        if (this.hierarchy != null)
            setHierarchy(this.hierarchy);
    }

    public void setPathId(int index, int value)
    {
        this.pathIds[index] = value;
    }

    public int getPathId(int index)
    {
        return this.pathIds[index];
    }

    public void setPath(int[] ids, String[] names)
    {
        if (ids == null)
        {
            return;
        }
        for (int i = 0; i < ids.length; i++)
        {
            setPathId(i, ids[i]);
            if (names != null)
            {
                setPathName(i, names[i]);
            }
        }
        for (int i = ids.length; i < getPathIds().length; i++)
            setPathId(i, -1);
        
        updateUi();
    }

    public void fireEvent(GwtEvent<?> event)
    {
        this.manager.fireEvent(event);
    }

    public String getPathName(int index)
    {
        return this.pathNames[index];
    }

    public void setPath(int index, int id, String name)
    {
        this.pathIds[index] = id;
        this.pathNames[index] = name;
    }

    public void setPathName(int index, String value)
    {
        this.pathNames[index] = value;
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<EmisEntity> handler)
    {
        return this.manager.addHandler(ValueChangeEvent.getType(), handler);
    }

    protected void getHierarchyEntities(EmisMetaEntity parentEntity, int parentId, StatusAsyncCallback<Map<Integer, String>> callback)
    { this.emis.getService().getHierarchyEntities(meta.getDatasetName(), getHierarchy().getName(), getDateIndex(), parentEntity, parentId, callback); }

    protected int getEntityTypeIndex(EmisMetaEntity entityType)
    {
        return entityType == null ? -1 : NamedUtil.findIndex(entityType.getName(), this.hierarchy.getEntityOrder());
    }

    protected boolean hasChildEntries(EmisMetaEntity entity)
    {
        int entityTypeIndex = getEntityTypeIndex(entity);
        if (this.entityType != null)
        {
            return entityTypeIndex < this.entityTypeIndex;
        }
        return entityTypeIndex + 1 < getSize();
    }

    protected String[] getUsedPathNames()
    {
        int size = getUsedSize();
        if (size == 0)
        {
            return null;
        }
        String[] result = new String[size];
        for (int i = 0; i < size; i++)
        {
            result[i] = getPathName(i);
        }
        return result;
    }

    protected int getSize()
    {
        return this.pathIds == null ? 0 : this.pathIds.length;
    }

    public int[] getUsedPathIds()
    {
        int size = getUsedSize();
        if (size == 0)
        {
            return null;
        }
        int[] result = new int[size];
        for (int i = 0; i < size; i++)
        {
            result[i] = getPathId(i);
        }
        return result;
    }

    protected int getUsedSize()
    {
        int size = 0;
        while ((size < this.pathIds.length) && (this.pathIds[size] != -1))
            size++;

        return size;
    }

    protected void fireValueChangeEvent(final EmisEntity entity)
    {
    	int index = 0; 
    	while (pathIds[index] == -1 || pathIds[index] == 0 && index < pathIds.length)
    		index++; 

    	if (index == pathIds.length)
    		return; 
    	
    	if (index > 0)
    	{
    		EmisEntity item = new Entity(hierarchy.getEntityOrder().get(index), pathIds[index]);

	    	// Find any missing path ids and names.  
	    	emis.getService().findEntityParents(meta.getDatasetName(), hierarchy.getName(), getDateIndex(), item, new StatusAsyncCallback<List<EmisEntity>>("Loading parents") {
				@Override
				public void onSuccess(List<EmisEntity> result) 
				{
					super.onSuccess(result);
					int index = 0; 
					for (EmisEntity parentEntity : result)
					{
						pathIds[index] = parentEntity.getId(); 
						pathNames[index] = parentEntity.getName(); 
						index++; 
					}

			    	ValueChangeEvent.fire(HierarchyBrowserBase.this, entity);
				}
	    	}); 
    	}
    	else
	    	ValueChangeEvent.fire(this, entity);
    }

    protected abstract void updateUi();
}
