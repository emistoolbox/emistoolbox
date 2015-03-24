package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.admin.DbMapEditor;
import com.emistoolbox.client.admin.ui.GwtUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import com.emistoolbox.common.model.mapping.impl.DbContextImpl;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MapUiDbContextEditor extends VerticalPanel implements HasValueChangeHandlers<DbContext>, DbMetaInfoAware
{
    private DbContext context;
    private ListBoxWithUserObjects<DbDataSourceConfig> uiDataSource = new ListBoxWithUserObjects<DbDataSourceConfig>();
    private ListBox uiQuery = new ListBox();
    private CheckBox uiLoop = null;  
    private DbMapEditor editor;
    private EmisMetaDateEnum loopEnum; 

    public MapUiDbContextEditor(DbMapEditor editor, EmisMetaDateEnum loopEnum) 
    {
        this.editor = editor;
        this.loopEnum = loopEnum; 
        
        setSpacing(2); 
        HorizontalPanel hp = new HorizontalPanel(); 
        hp.add(this.uiDataSource);
        HTML uiRefresh = new HTML(" [refresh]"); 
        uiRefresh.addStyleName("pointer");
        uiRefresh.addStyleName("value"); 
        uiRefresh.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { ValueChangeEvent.fire(MapUiDbContextEditor.this, MapUiDbContextEditor.this.get()); }
        }); 

        hp.add(uiRefresh); 
        
        add(hp);
        add(this.uiQuery);
        
        if (loopEnum != null)
        {
            uiLoop = new CheckBox(); 
            uiLoop.setText("Loop across '" + loopEnum.getName() + "' (using context variable '" + loopEnum.getName().toLowerCase() + ")");  
            add(this.uiLoop);
        }

        ChangeHandler changeHandler = new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                ValueChangeEvent.fire(MapUiDbContextEditor.this, MapUiDbContextEditor.this.get());
            }
        };
        this.uiDataSource.addChangeHandler(changeHandler);
        this.uiQuery.addChangeHandler(changeHandler);
    }

    public void setDataSourceConfigs(List<DbDataSourceConfig> configs)
    {
        DbDataSourceConfig value = (DbDataSourceConfig) this.uiDataSource.getValue();
        for (DbDataSourceConfig config : configs)
        {
            this.uiDataSource.add(config.getName(), config);
        }
        if ((value == null) && (configs.size() == 1))
        {
            value = (DbDataSourceConfig) configs.get(0);
        }
        this.uiDataSource.setValue(value);
        if ((value == null) && (configs.size() == 1))
            this.uiDataSource.setValue(configs.get(0));
    }

    public void set(DbContext context)
    {
        if (context == null)
            context = new DbContextImpl();
        this.context = context;

        this.uiDataSource.setValue(context.getDataSource());
        if (uiLoop != null)
            uiLoop.setValue(this.context.getLoopEnum() !=  null); 
        
        GwtUtils.setListValueWithAdd(this.uiQuery, context.getQuery());
    }

    public DbContext get()
    {
        if (this.context == null)
            this.context = new DbContextImpl();
        
        this.context.setDataSource((DbDataSourceConfig) this.uiDataSource.getValue());
        this.context.setQuery(GwtUtils.getListValue(this.uiQuery));
        
        if (uiLoop != null)
        {
            if (uiLoop.getValue() != null)
            {
                this.context.setLoopVariable(uiLoop.getValue() ? loopEnum.getName().toLowerCase() : null);
                this.context.setLoopEnum(uiLoop.getValue() ? loopEnum : null); 
            }
            else
            {
                this.context.setLoopVariable(null); 
                this.context.setLoopEnum(null); 
            }
        }
        
        return this.context;
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DbContext> handler)
    {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public void updateDbMetaInfo()
    {
        Map map = null;
        if (this.uiDataSource.getValue() != null)
        {
            map = this.editor.getDbMetaInfo((DbDataSourceConfig) this.uiDataSource.getValue());
        }
        updateDbMetaInfo(get(), map);
    }

    public void updateDbMetaInfo(DbContext dbContext, Map<String, List<String>> dbMetaInfo)
    {
        if (get() != dbContext)
        {
            return;
        }
        List<String> keys = new ArrayList<String>();
        if (dbMetaInfo != null)
            keys.addAll(dbMetaInfo.keySet());
        Collections.sort(keys);

        String value = GwtUtils.getListValue(this.uiQuery);
        this.uiQuery.clear();
        for (String key : keys)
            this.uiQuery.addItem(key);
        GwtUtils.setListValue(this.uiQuery, value);
    }
}
