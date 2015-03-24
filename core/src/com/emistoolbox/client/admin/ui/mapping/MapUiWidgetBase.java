package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

public abstract class MapUiWidgetBase extends FlexTable implements MapUiWidget
{
    private EmisToolboxServiceAsync service;
    private DbContext context;
    private EmisMetaData metaField;
    private MapUiConstraint constraint;
    private String dataset; 

    public MapUiWidgetBase(EmisToolboxServiceAsync service, String dataset) 
    {
        this.service = service;
        this.dataset = dataset; 
    }

    public void setDbContext(DbContext context)
    {
        this.context = context;
    }

    public void setMetaContext(EmisMetaData field)
    {
        this.metaField = field;
    }

    protected void notify(Widget widget)
    {
        if ((widget instanceof MapUiWidget))
            notify((MapUiWidget) widget);
    }

    protected void notify(MapUiWidget widget)
    {
        if (widget == null)
        {
            return;
        }
        if (this.context != null)
            widget.setDbContext(this.context);
        if (this.metaField != null)
            widget.setMetaContext(this.metaField);
    }

    public EmisToolboxServiceAsync getService()
    {
        return this.service;
    }

    public DbContext getDbContext()
    {
        return this.context;
    }

    public EmisMetaData getMetaContext()
    { return this.metaField; }

    public MapUiConstraint getConstraint()
    {
        return this.constraint;
    }

    public void setConstraint(MapUiConstraint constraint)
    {
        this.constraint = constraint;
    }
    
    public String getDataset()
    { return dataset; } 
}
