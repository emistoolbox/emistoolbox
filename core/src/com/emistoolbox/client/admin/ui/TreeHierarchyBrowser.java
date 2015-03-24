package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.StatusAsyncCallback;
import com.emistoolbox.client.ui.results.IHierarchyBrowser;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TreeHierarchyBrowser extends HierarchyBrowserBase implements IHierarchyBrowser, OpenHandler<TreeItem>, CloseHandler<TreeItem>, SelectionHandler<TreeItem>
{
    private Tree ui = new Tree();
    
    public TreeHierarchyBrowser(EmisToolbox emis, List<EmisEntity> rootEntities) 
    {
        super(emis, rootEntities);
        this.ui.addOpenHandler(this);
        this.ui.addCloseHandler(this);
        this.ui.addSelectionHandler(this);
    }

    public Widget getWidget()
    { return this.ui; }

    public void setHierarchy(EmisMetaHierarchy hierarchy)
    { super.setHierarchy(hierarchy); }

    protected void updateUi()
    { updateUi(null); }

    private void updateUi(final TreeItem parentItem)
    {
        if (parentItem == null)
        {
        	if (getRootEntities() != null && getRootEntities().size() > 0)
        	{
        		emis.getService().getEntityNames(meta.getDatasetName(), getHierarchy().getName(), getRootEntities(), new StatusAsyncCallback<List<EmisEntity>>("Loading root entity names") {

					@Override
					public void onSuccess(List<EmisEntity> result) 
					{
						setRootEntities(sortAndFilterEntities(result)); 
		        		addRootEntities(); 
					}
        		});
        		
        		return; 
        	}

            getHierarchyEntities(null, 0, new StatusAsyncCallback<Map<Integer, String>>(null) {
                public void onSuccess(Map<Integer, String> result)
                {
                    super.onSuccess(result);
                    TreeHierarchyBrowser.this.updateItems(parentItem, result);
                }
            });
        }
        else
        {
            EmisEntity entity = getEntity(parentItem);
            parentItem.removeItems();
            getHierarchyEntities(entity.getEntityType(), entity.getId(), new StatusAsyncCallback<Map<Integer, String>>(null) {
                public void onSuccess(Map<Integer, String> result)
                {
                    super.onSuccess(result);
                    TreeHierarchyBrowser.this.updateItems(parentItem, result);
                }
            });
        }
    }

    private List<EmisEntity> sortAndFilterEntities(List<EmisEntity> entities)
    {
    	List<EmisEntity> result = new ArrayList<EmisEntity>(); 
    	result.addAll(entities); 
    	
    	Iterator<EmisEntity> iter = result.iterator(); 
    	while (iter.hasNext())
    	{
    		if (NamedUtil.findIndex(iter.next().getEntityType(), getHierarchy().getEntityOrder()) == -1)
    			iter.remove(); 
    	}
    	
    	Comparator<EmisEntity> comparator = new Comparator<EmisEntity>() {
			@Override
			public int compare(EmisEntity entity1, EmisEntity entity2) 
			{
				int pos1 = NamedUtil.findIndex(entity1.getEntityType(), getHierarchy().getEntityOrder()); 
				int pos2 = NamedUtil.findIndex(entity2.getEntityType(), getHierarchy().getEntityOrder());
				
				int result = pos1 == pos2 ? 0 : pos1 < pos2 ? -1 : 1;  
				if (result != 0)
					return result; 
				
				return entity1.getName().compareTo(entity2.getName());  
			}
    	}; 
    	
    	Collections.sort(result, comparator);

    	return result; 
    }
    
    private void addRootEntities()
    {
    	this.ui.clear(); 
    	
    	// TODO - check hierarchy? 
    	// TODO - sort items - by type, then name.
    	for (EmisEntity entity : getRootEntities())
    	{
    		TreeItem item = new TreeItem(entity.getEntityType().getName() + ": " + entity.getName());
    		item.setUserObject(entity);
    		this.ui.addItem(item); 
    		if (hasChildEntries(entity.getEntityType()))
    			item.addItem(new TreeItem("")); 
    	}
    }
    
    private void updateItems(TreeItem parent, Map<Integer, String> children)
    {
        int entityTypeId = parent == null ? 0 : getEntityTypeIndex(getEntityType(parent)) + 1;
        EmisMetaEntity entityType = (EmisMetaEntity) getHierarchy().getEntityOrder().get(entityTypeId);

        List<EmisEntity> entities = new ArrayList<EmisEntity>();
        for (Map.Entry<Integer, String> item : children.entrySet())
        {
            EmisEntity entity = new Entity(entityType, item.getKey().intValue());
            entity.setName((String) item.getValue());
            entities.add(entity);
        }

        int selected = getPathId(entityTypeId);

        NamedUtil.sort(entities);

        TreeItem firstItem = null; 
        
        for (EmisEntity entity : entities)
        {
            TreeItem item = new TreeItem(entity.getName());
            if (firstItem == null)
                firstItem = item; 
            
            item.setUserObject(entity);
            if (hasChildEntries(entity.getEntityType()))
                item.addItem(new TreeItem(""));

            if (parent == null)
                this.ui.addItem(item);
            else
                parent.addItem(item);

            if (entity.getId() == selected)
            {
                item.setSelected(true);
                if (entityTypeId + 1 < getPathIds().length)
                    updateUi(item);
                
                this.ui.ensureSelectedItemVisible();
            }
        }

        if (parent != null)
            parent.setState(true); 
            

        if (entities.size() == 1)
        {
            // There is only a single item. We can either expand (if there are children) or select the node. 
            // 'item' holds the TreeItem added.  
            EmisEntity entity = entities.get(0); 
            if (hasChildEntries(entity.getEntityType()))
                expand(firstItem);
            else
                select(firstItem); 
        }
    }

    public void onClose(CloseEvent<TreeItem> event)
    {
        TreeItem item = (TreeItem) event.getTarget();
        item.removeItems();
        if (getEntityTypeIndex(getEntityType(item)) + 1 < getSize())
            item.addItem(new TreeItem(""));
    }

    public void onOpen(OpenEvent<TreeItem> event)
    { expand((TreeItem) event.getTarget()); }

    private void expand(TreeItem item)
    {
        int pathIndex = getEntityTypeIndex(getEntityType(item));
        EmisEntity entity = getEntity(item);
        setPath(pathIndex, entity.getId(), entity.getName());

        updateUi(item);
    }

    public void onSelection(SelectionEvent<TreeItem> event)
    { select((TreeItem) event.getSelectedItem()); }
    
    public void select(TreeItem item)
    {
        int entityTypeId = getEntityTypeIndex(getEntityType(item));

        if (hasAnySelection() || getEntityType() == null || getEntityTypeIndex() == entityTypeId)
        {
            int pathIndex = getEntityTypeIndex(getEntityType(item));
            EmisEntity entity = getEntity(item);
            setPath(pathIndex, entity.getId(), entity.getName());

            fireValueChangeEvent(getEntity(item)); 
        }
        else if (hasChildEntries(getEntityType(item)))
        {
            expand(item);
        }
        else
        {
            item.setSelected(false);
        }
    }

    private EmisEntity getEntity(TreeItem item)
    {
        return item == null ? null : (EmisEntity) item.getUserObject();
    }

    private EmisMetaEntity getEntityType(TreeItem item)
    {
        EmisEntity entity = getEntity(item);
        if (entity == null)
        {
            return null;
        }
        return entity.getEntityType();
    }
}
