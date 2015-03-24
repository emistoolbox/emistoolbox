package com.emistoolbox.client.admin;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.GwtUtils;
import com.emistoolbox.client.admin.ui.mapping.DbMetaInfoAware;
import com.emistoolbox.client.admin.ui.mapping.MapUiDataSourceConfig;
import com.emistoolbox.client.admin.ui.mapping.MapUiEntityEditor;
import com.emistoolbox.client.admin.ui.mapping.MapUiGisEntityEditor;
import com.emistoolbox.client.admin.ui.mapping.MapUiHierarchyEntryEditor;
import com.emistoolbox.client.admin.ui.mapping.MapUiTableOps;
import com.emistoolbox.client.ui.ActionPanel;
import com.emistoolbox.client.ui.BlockingScreen;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.EmisDbMap;
import com.emistoolbox.common.model.mapping.EmisDbMapBase;
import com.emistoolbox.common.model.mapping.EmisEntityDbMap;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMap;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMapEntry;
import com.emistoolbox.common.model.mapping.EntityBaseDbMap;
import com.emistoolbox.common.model.mapping.GisEntityDbMap;
import com.emistoolbox.common.model.mapping.impl.EmisEntityDbMapImpl;
import com.emistoolbox.common.model.mapping.impl.EmisHierarchyDbMapEntryImpl;
import com.emistoolbox.common.model.mapping.impl.EmisHierarchyDbMapImpl;
import com.emistoolbox.common.model.mapping.impl.GisEntityDbMapImpl;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEntity.EmisGisType;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.NamedIndexList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DbMapEditor extends FlexTable implements EmisEditor<EmisDbMap>
{
    private EmisToolbox emis;
    private EmisDbMap dbMap;
    private Tree mapTree = new Tree();
    private ScrollPanel uiTreeScroll = new ScrollPanel(); 
    private ScrollPanel uiValidationScroll = new ScrollPanel();
    private VerticalPanel uiValidation = new VerticalPanel();

    private Map<DbDataSourceConfig, Map> dbMetaData = new HashMap();

    public DbMapEditor(EmisToolbox emis) {
        this.emis = emis;
        setWidth("100%");

        mapTree.setStylePrimaryName("emisTree");
        mapTree.addStyleName("lightBackground");
        mapTree.setWidth("100%");
        uiTreeScroll.setWidget(mapTree);
        uiTreeScroll.setStyleName("select-list"); 
        uiTreeScroll.setHeight("250px");

        uiValidation.setWidth("100%");
        uiValidation.setSpacing(2);
        uiValidationScroll.setWidget(this.uiValidation);
        uiValidationScroll.setWidth("100%");
        uiValidationScroll.setStyleName("enumMapping");
        uiValidationScroll.setHeight("150px");

        this.mapTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            public void onSelection(SelectionEvent<TreeItem> event)
            { DbMapEditor.this.selectItem(); }
        });
    }

    private void selectItem()
    {
        final TreeItem item = this.mapTree.getSelectedItem();

        Object userObject = item == null ? null : item.getUserObject();
        if (userObject == null)
        {
            if ((item != null) && (Message.messageAdmin().dbmapDataSources().equals(item.getText())))
            {
                HorizontalPanel hp = new HorizontalPanel();
                hp.setSpacing(2);

                hp.add(EmisUtils.init(new PushButton(Message.messageAdmin().dbmapBtnAddDataSource(), new ClickHandler() {
                    public void onClick(ClickEvent event)
                    {
                        TreeItem newItem = new TreeItem(Message.messageAdmin().dbmapNewDataSource());
                        item.addItem(newItem);
                        DbMapEditor.this.mapTree.setSelectedItem(newItem);
                        DbMapEditor.this.mapTree.ensureSelectedItemVisible();

                        DbMapEditor.this.show((DbDataSourceConfig) null, newItem);
                    }
                }), 120));

                hp.add(EmisUtils.init(new PushButton(Message.messageAdmin().dbmapBtnRefresh(), new ClickHandler() {
                    public void onClick(ClickEvent event)
                    {
                        DbMapEditor.this.loadDataSourceInfo();
                    }
                }), 120));

                show(hp);
            }
            else
            {
                show((Widget) null);
            }
            return;
        }

        if ((userObject instanceof EmisEntityDbMap))
            show((EmisEntityDbMap) userObject);
        else if ((userObject instanceof GisEntityDbMap))
            show((GisEntityDbMap) userObject);
        else if ((userObject instanceof EmisHierarchyDbMap))
            show((EmisHierarchyDbMap) userObject);
        else if ((userObject instanceof EmisHierarchyDbMapEntry))
            show((EmisHierarchyDbMapEntry) userObject);
        else if ((userObject instanceof DbDataSourceConfig))
            show((DbDataSourceConfig) userObject, item);
        else if ((userObject instanceof MapUiDataSourceConfig))
            show((MapUiDataSourceConfig) userObject, item);
        else if ((userObject instanceof EmisMetaEntity))
            show((EmisMetaEntity) userObject, item);

        updateValidation();
    }
    
    public void deleteDataSource()
    {
        // Find current selection. 
        final TreeItem item = this.mapTree.getSelectedItem();
        Object userObject = item == null ? null : item.getUserObject();
        
        DbDataSourceConfig config = null; 
        if (userObject instanceof DbDataSourceConfig)
            config = (DbDataSourceConfig) userObject; 
        else if (userObject instanceof MapUiDataSourceConfig)
            config = ((MapUiDataSourceConfig) userObject).get(); 
        
        if (config == null)
            return; 

        Iterator<DbDataSourceConfig> iter = dbMap.getDataSources().iterator(); 
        while (iter.hasNext())
        {
            if (iter.next() == config)
                iter.remove(); 
        }
        
        removeDataSource(config, dbMap.getEntityMappings());  
        for (EmisHierarchyDbMap map : dbMap.getHierarchyMappings())
            removeDataSource(config, map.getMappings()); 
        removeDataSource(config, dbMap.getGisEntityMappings()); 
        
        item.remove(); 
        clearCell(2, 1); 

        // Need to reload the tree. 
        set(dbMap); 
    }

    private static<T extends EmisDbMapBase> void removeDataSource(DbDataSourceConfig config, List<T> items)
    {
        if (items == null)
            return; 
        
        Iterator<T> iter = items.iterator(); 
        while (iter.hasNext())
        {
            DbContext dbContext = iter.next().getDbContext(); 
            if (dbContext != null && dbContext.getDataSource() == config)
                iter.remove(); 
        }
    }
    
    public Map<String, List<String>> getDbMetaInfo(DbDataSourceConfig config)
    {
        if (config == null)
            return null;
        return (Map) this.dbMetaData.get(config);
    }

    public void set(EmisDbMap dbMap)
    {
        this.dbMap = dbMap;

        this.mapTree.clear();

        TreeItem section = this.mapTree.addItem(Message.messageAdmin().dbmapDataSources());
        for (DbDataSourceConfig config : dbMap.getDataSources())
        {
            TreeItem dataSourceItem = section.addItem(config.getName());
            dataSourceItem.setUserObject(config);
            loadDataSourceInfo(config);
        }

        section = this.mapTree.addItem(Message.messageAdmin().dbmapLocations());
        for (Iterator i$ = dbMap.getMetaData().getEntities().iterator(); i$.hasNext();)
        {
            EmisMetaEntity entity = (EmisMetaEntity) i$.next();
            TreeItem entityItem = section.addItem(entity.getName());
            entityItem.setUserObject(entity);

            if (entity.getGisType() != EmisMetaEntity.EmisGisType.NONE)
            {
                TreeItem item = entityItem.addItem(Message.messageAdmin().meTabGis() + " "
                        + (entity.getGisType() == EmisMetaEntity.EmisGisType.COORDINATE ? Message.messageAdmin().gisTypeCoordinate() : Message.messageAdmin().gisTypePolygon()));
                GisEntityDbMap map = dbMap.findGisMap(entity);
                if (map == null)
                {
                    map = new GisEntityDbMapImpl();
                    map.setEmisMetaEntity(entity);
                    dbMap.setGisMap(map);
                }

                item.setUserObject(map);
            }

            for (EmisMetaDateEnum dateType : dbMap.findDateTypes(entity))
            {
                boolean anyEntries = false;
                for (EmisEntityDbMap map : dbMap.find(entity, dateType))
                {
                    TreeItem item = entityItem.addItem(""); 
                    item.setHTML(dateType.getName() + " - " + getNameSummary(map.getFieldAccess()));
                    item.setUserObject(map);
                    anyEntries = true;
                }

                if (!anyEntries)
                {
                    EmisEntityDbMap entityMap = new EmisEntityDbMapImpl();
                    entityMap.setEmisMetaEntity(entity);
                    entityMap.setDateEnum(dateType);

                    TreeItem item = entityItem.addItem(dateType.getName());
                    item.setUserObject(entityMap);
                }
            }
        }
        section = this.mapTree.addItem(Message.messageAdmin().dbmapHeirarchies());
        for (Iterator i$ = dbMap.getMetaData().getHierarchies().iterator(); i$.hasNext();)
        {
            EmisMetaHierarchy hierarchy = (EmisMetaHierarchy) i$.next();
            TreeItem hierarchyItem = section.addItem(hierarchy.getName());
            EmisHierarchyDbMap hierarchyMap = dbMap.find(hierarchy);
            if (hierarchyMap == null)
            {
                hierarchyMap = new EmisHierarchyDbMapImpl();
                hierarchyMap.setHierarchy(hierarchy);
            }
            hierarchyItem.setUserObject(hierarchyMap);

            EmisMetaEntity parentEntity = null;
            for (EmisMetaEntity entity : hierarchy.getEntityOrder())
                try
                {
                    if (parentEntity == null)
                    {
                        parentEntity = entity;
                        continue;
                    }
                    TreeItem map = hierarchyItem.addItem(entity.getName() + " > " + parentEntity.getName());

                    EmisHierarchyDbMapEntry entry = dbMap.find(hierarchy, parentEntity, entity);

                    if (entry == null)
                    {
                        entry = new EmisHierarchyDbMapEntryImpl();
                        entry.setChildEntity(entity);
                        entry.setParentEntity(parentEntity);
                        entry.setDateType(dbMap.getMetaData().getDefaultDateType());
                    }

                    map.setUserObject(entry);
                }
                finally
                {
                    parentEntity = entity;
                }
        }


        VerticalPanel vp = new VerticalPanel();
        vp.add(EmisToolbox.metaResultEditFrame(uiTreeScroll));
        vp.add(new HTML("&nbsp;")); 
        vp.add(EmisToolbox.metaResultEditFrame(uiValidationScroll));
        setWidget(2, 0, vp); 
        getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
        getCellFormatter().setVerticalAlignment(2, 1, HasVerticalAlignment.ALIGN_TOP);

        selectItem();
        updateValidation();
    }

    private void updateDbMetaInfo()
    {
        Widget w = getWidget(2, 1);
        if ((w instanceof DbMetaInfoAware))
        {
            ((DbMetaInfoAware) w).updateDbMetaInfo();
        }

        for (int i = 0; i < this.mapTree.getItemCount(); i++)
            updateDbMetaInfo(this.mapTree.getItem(i));
    }

    private void updateDbMetaInfo(TreeItem item)
    {
        if ((item.getUserObject() instanceof DbDataSourceConfig))
            item.setText(((DbDataSourceConfig) item.getUserObject()).getName());
        else if ((item.getUserObject() instanceof MapUiDataSourceConfig))
        {
            item.setText(((MapUiDataSourceConfig) item.getUserObject()).get().getName());
        }
        else
            for (int i = 0; i < item.getChildCount(); i++)
                updateDbMetaInfo(item.getChild(i));
    }

    private void show(Widget w)
    {
        if (w == null)
            w = new Label();

        commit();
        setWidget(2, 1, w);
    }

    private void show(final EmisMetaEntity entity, final TreeItem item)
    {
        commit();

        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(2);

        Set<EmisMetaDateEnum> dateTypes = getDateTypes(entity);
        for (final EmisMetaDateEnum dateType : dateTypes)
        {
            PushButton btn = new PushButton(Message.messageAdmin().dbmapBtnAddMapping(dateType.getName()), new ClickHandler() {
                public void onClick(ClickEvent event)
                {
                    EmisEntityDbMapImpl result = new EmisEntityDbMapImpl();
                    result.setEmisMetaEntity(entity);
                    result.setDateEnum(dateType);

                    TreeItem newItem = item.addItem(dateType.getName());
                    newItem.setUserObject(result);
                    item.getTree().setSelectedItem(newItem);
                    item.getTree().ensureSelectedItemVisible();

                    DbMapEditor.this.show(result);
                }
            });
            EmisUtils.init(btn, 150);
            vp.add(btn);
        }

        setWidget(2, 1, vp);
    }

    private Set<EmisMetaDateEnum> getDateTypes(EmisMetaEntity entity)
    {
        Set result = new HashSet();
        for (EmisMetaData data : entity.getData())
        {
            result.add(data.getDateType());
        }
        return result;
    }

    private void show(EmisEntityDbMap entityMap)
    {
        commit();
        MapUiEntityEditor editor = new MapUiEntityEditor(this, this.emis.getService(), new MapUiTableOps() {
            public void add(String name, Object userObject)
            {
            }

            public void del()
            {
                DbMapEditor.this.setWidget(2, 1, new Label());

                TreeItem item = DbMapEditor.this.mapTree.getSelectedItem();
                TreeItem nextItem = item.getParentItem();
                nextItem.removeItem(item);
                if (nextItem != null)
                    DbMapEditor.this.mapTree.setSelectedItem(nextItem);
            }
        });
        editor.set(entityMap);
        setWidget(2, 1, editor);
    }

    private void show(GisEntityDbMap gisEntityMap)
    {
        commit();
        MapUiGisEntityEditor editor = new MapUiGisEntityEditor(this, this.emis.getService());
        editor.set(gisEntityMap);
        setWidget(2, 1, editor);
    }

    private void show(EmisHierarchyDbMap hierarchyMap)
    {
        commit();
        setWidget(2, 1, new Label(Message.messageAdmin().dbmapLabelConfigureChildElements()));
    }

    private void show(EmisHierarchyDbMapEntry hierarchyEntry)
    {
        commit();
        MapUiHierarchyEntryEditor editor = new MapUiHierarchyEntryEditor(this, this.emis.getService());
        editor.set(hierarchyEntry);
        setWidget(2, 1, editor);
    }

    private void show(DbDataSourceConfig config, TreeItem item)
    {
        commit();
        MapUiDataSourceConfig editor = new MapUiDataSourceConfig(emis.getService(), getDataset());
        editor.set(config);
        item.setUserObject(editor);

        showDataSourceConfigEditor(editor); 
    }
    
    private void show(MapUiDataSourceConfig editor, TreeItem item)
    {
        commit(); 
        showDataSourceConfigEditor(editor); 
    }
    
    private void showDataSourceConfigEditor(final MapUiDataSourceConfig editor)
    {
        PushButton btnDelete = EmisUtils.init(new PushButton("Delete Data Source", new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                if (!Window.confirm("Are you sure you want to delete this data source and all mappings that use it?"))
                    return; 
                
                deleteDataSource();
            }
        }), 150); 

        VerticalPanel vp = new VerticalPanel(); 
        vp.add(btnDelete); 
        vp.add(editor); 
        
        setWidget(2, 1, vp);
    }

    private BlockingScreen getBlockingScreen()
    {
        final ActionPanel actions = new ActionPanel();
        BlockingScreen screen = new BlockingScreen(actions.detachStatus(), true) {
            public void cleanup()
            {
                actions.attachStatus();
            }
        };
        actions.startProgress();
        actions.setMessage(Message.messageAdmin().dbmapDataSourceConnectingTitle(), Message.messageAdmin().dbmapDataSourceConnectingMessage());

        return screen;
    }

    private void loadDataSourceInfo()
    {
        BlockingScreen screen = getBlockingScreen();
        for (DbDataSourceConfig config : this.dbMap.getDataSources())
        {
            loadDataSourceInfo(config, screen);
        }
        screen.finished();
    }

    private void loadDataSourceInfo(DbDataSourceConfig config)
    {
        loadDataSourceInfo(config, null);
    }

    private void loadDataSourceInfo(final DbDataSourceConfig config, BlockingScreen screen)
    {
        boolean callFinished = false;
        if (screen == null)
        {
            screen = getBlockingScreen();
            callFinished = true;
        }

        this.emis.getService().getDataSourceInfo(config, getDataset(), screen.getCallback(new AsyncCallback<Map<String, List<String>>>() {
            public void onFailure(Throwable caught)
            {
                Window.alert(Message.messageAdmin().dbmapAlertFailToConnect(config.getName()) + "\n\n" + caught.getMessage());
                DbMapEditor.this.dbMetaData.put(config, null);
                config.setHasConnected(false);
                DbMapEditor.this.updateDbMetaInfo();
            }

            public void onSuccess(Map<String, List<String>> result)
            {
                DbMapEditor.this.dbMetaData.put(config, result);
                config.setHasConnected(true);
                DbMapEditor.this.updateDbMetaInfo();
            }
        }));
        if (callFinished)
            screen.finished();
    }

    public void commit()
    {
        Widget w = null;
        if ((getRowCount() >= 3) && (getCellCount(2) >= 2))
        {
            w = getWidget(2, 1);
        }
        if (w == null)
        {
            return;
        }
        if ((w instanceof MapUiDataSourceConfig))
        {
            TreeItem item = GwtUtils.findTreeItem(this.mapTree, w);
            if (item != null)
            {
                DbDataSourceConfig config = ((MapUiDataSourceConfig) w).get();
                if (config != null)
                {
                    item.setText(config.getName());
                    this.dbMetaData.remove(config);
                    loadDataSourceInfo(config);
                }
                else
                {
                    TreeItem parentItem = item.getParentItem();
                    parentItem.removeItem(item);
                    this.mapTree.setSelectedItem(parentItem);
                }
            }
        }
        else if ((w instanceof EmisEditor))
        {
            ((EmisEditor) w).commit();
        }
        if ((w instanceof MapUiEntityEditor))
        {
            MapUiEntityEditor entityMapEditor = (MapUiEntityEditor) w;
            EmisEntityDbMap entityMap = (EmisEntityDbMap) entityMapEditor.get();

            TreeItem item = GwtUtils.findTreeItem(this.mapTree, entityMap);
            if (item != null)
                item.setHTML(entityMap.getDateEnum().getName() + " - " + getNameSummary(entityMap.getFieldAccess()));
        }
    }

    private String getNameSummary(Map<String, DbRowAccess> mapping)
    {
        if (mapping == null)
            return "";

        StringBuffer result = new StringBuffer();
        int count = 0; 
        for (String field : mapping.keySet())
        {
            if (count > 0)
            {
                result.append(", ");
                if (count % 2 == 0)
                    result.append("<br>&nbsp;&nbsp;"); 
            }
            count++; 
            
            result.append(field);
        }

        return result.toString();
    }

    public EmisDbMap get()
    {
        List dataSources = new ArrayList();
        List entityMappings = new ArrayList();
        List gisEntityMappings = new ArrayList();
        List hierarchyMappings = new ArrayList();

        for (int i = 0; i < this.mapTree.getItemCount(); i++)
        {
            addTreeItem(this.mapTree.getItem(i), dataSources, entityMappings, gisEntityMappings, hierarchyMappings);
        }
        this.dbMap.setDataSources(dataSources);
        this.dbMap.setEntityMappings(entityMappings);
        this.dbMap.setGisEntityMappings(gisEntityMappings);
        this.dbMap.setHierarchyMappings(hierarchyMappings);

        return this.dbMap;
    }

    private void addTreeItem(TreeItem item, List<DbDataSourceConfig> dataSources, List<EmisEntityDbMap> entityMappings, List<GisEntityDbMap> gisEntityMappings, List<EmisHierarchyDbMap> hierarchyMappings)
    {
        boolean recurse = true;
        Object userObject = item.getUserObject();
        if ((userObject instanceof DbDataSourceConfig))
        {
            dataSources.add((DbDataSourceConfig) userObject);
        }
        else if ((userObject instanceof MapUiDataSourceConfig))
        {
            DbDataSourceConfig config = ((MapUiDataSourceConfig) userObject).get();
            if (config != null)
                dataSources.add(config);
        }
        else if ((userObject instanceof EmisEntityDbMap))
        {
            entityMappings.add((EmisEntityDbMap) userObject);
        }
        else if ((userObject instanceof GisEntityDbMap))
        {
            gisEntityMappings.add((GisEntityDbMap) userObject);
        }
        else if ((userObject instanceof EmisHierarchyDbMap))
        {
            addHierarchyItems((EmisHierarchyDbMap) userObject, item);
            hierarchyMappings.add((EmisHierarchyDbMap) userObject);
            recurse = false;
        }
        else if ((userObject instanceof EmisHierarchyDbMapEntry))
        {
            throw new IllegalArgumentException(Message.messageAdmin().dbmapErrorNotParentOfEmisHierarchyDbMap());
        }
        if (recurse)
            for (int i = 0; i < item.getChildCount(); i++)
                addTreeItem(item.getChild(i), dataSources, entityMappings, gisEntityMappings, hierarchyMappings);
    }

    private void addHierarchyItems(EmisHierarchyDbMap parent, TreeItem item)
    {
        List result = parent.getMappings();
        if (result == null)
            result = new ArrayList();
        else
        {
            result.clear();
        }
        for (int i = 0; i < item.getChildCount(); i++)
        {
            TreeItem child = item.getChild(i);
            if ((child.getUserObject() == null) || (!(child.getUserObject() instanceof EmisHierarchyDbMapEntry)))
            {
                continue;
            }
            result.add((EmisHierarchyDbMapEntry) child.getUserObject());
        }

        parent.setMappings(result);
    }

    private void updateValidation()
    {
        MapValidator validation = new MapValidator(this.dbMap.getMetaData());
        validation.validate(get());

        Map<Object, String> messages = validation.getMessages();
        this.uiValidation.clear();
        HTML title = new HTML(Message.messageAdmin().dbmapErrorMissingConfigurations());
        title.setStyleName("title");
        this.uiValidation.add(title);

        for (final Map.Entry<Object, String> entry : messages.entrySet())
        {
            HTML label = new HTML("<div class='sectionBlue'>" + validation.getContextName(entry.getKey()) + "</div>" + entry.getValue() + "<hr>");

            label.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event)
                { DbMapEditor.this.showTreeItem(entry.getKey()); }
            });
            
            this.uiValidation.add(label);
        }
    }

    private void showTreeItem(Object userObject)
    {
        TreeItem item = GwtUtils.findTreeItem(this.mapTree, userObject);
        if (item != null)
        {
            this.mapTree.setSelectedItem(item);
            this.mapTree.ensureSelectedItemVisible();
        }
        else
        {
            Window.alert(userObject.toString());
        }
    }
    
    public String getDataset()
    { return dbMap.getMetaData().getDatasetName(); }
}
