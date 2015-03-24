package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.client.admin.DbMapEditor;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.EmisEntityDbMap;
import com.emistoolbox.common.model.mapping.EntityBaseDbMap;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;
import java.util.Map;

public abstract class MapUiEntityBaseEditor<T extends EntityBaseDbMap> extends MapUiEditorBase<T> implements DbMetaInfoAware
{
    private MapUiDbContextEditor dbContextEditor;
    private DbMapEditor editor;
    private PushButton uiDelButton;
    private int idFieldRow;

    public MapUiEntityBaseEditor(EmisToolboxServiceAsync service, DbMapEditor editor, PushButton uiDelButton) 
    {
        super(service, editor.getDataset());
        this.editor = editor;
        this.uiDelButton = uiDelButton;
    }

    public void set(T map)
    {
        super.set(map);
        updateUi();
    }

    protected void updateUi()
    {
        int row = 0;

        if (this.uiDelButton != null)
        {
            HorizontalPanel hp = new HorizontalPanel();
            hp.setSpacing(2);
            hp.add(this.uiDelButton);
            setWidget(row, 1, hp);
        }

        row++;

        EmisMetaDateEnum dateEnum = null; 
        EntityBaseDbMap baseDbMap = ((EntityBaseDbMap) get()); 
        if (baseDbMap instanceof EmisEntityDbMap)
            dateEnum = ((EmisEntityDbMap) baseDbMap).getDateEnum(); 
        
        getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
        setHTML(row, 0, Message.messageAdmin().mueeHtmlDataSource() + ":");
        EmisToolbox.css(this, row, 0, "sectionBlue");
        this.dbContextEditor = MapUiUtil.getMapUiAsWidget(baseDbMap.getDbContext(), dateEnum, this.editor);
        this.dbContextEditor.addValueChangeHandler(new ValueChangeHandler<DbContext>() {
            public void onValueChange(ValueChangeEvent<DbContext> event)
            {
                MapUiEntityBaseEditor.this.updateDbMetaInfo();
            }
        });
        this.dbContextEditor.updateDbMetaInfo();

        setWidget(row, 1, this.dbContextEditor);
        EmisToolbox.css(this, row, 1, "adminEdit");
        row++;

        this.idFieldRow = row;
        getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);

        setHTML(row, 0, Message.messageAdmin().mueeIdName(baseDbMap.getEntity().getName()) + ":");
        EmisToolbox.css(this, row, 0, "sectionBlue");

        setWidget(row, 1, updateDbMetaInfo(MapUiUtil.getMapUiAsWidget(baseDbMap.getIdAccess(), getService(), getDataset())));
        EmisToolbox.css(this, row, 1, "adminEdit");
        row++;

        getFlexCellFormatter().setColSpan(row, 0, 2);
        setHTML(row, 0, "<hr>");
        row++;
    }

    public void updateDbMetaInfo()
    {
        this.dbContextEditor.updateDbMetaInfo();

        DbContext dbContext = this.dbContextEditor.get();
        updateDbMetaInfo(dbContext, this.editor.getDbMetaInfo(dbContext == null ? null : dbContext.getDataSource()));
    }

    public Widget updateDbMetaInfo(Widget aware)
    {
        DbContext dbContext = this.dbContextEditor.get();
        if ((aware instanceof DbMetaInfoAware))
        {
            ((DbMetaInfoAware) aware).updateDbMetaInfo(dbContext, this.editor.getDbMetaInfo(dbContext == null ? null : dbContext.getDataSource()));
        }
        return aware;
    }

    public int getIdFieldRow()
    {
        return this.idFieldRow;
    }

    public void updateDbMetaInfo(DbContext dbContext, Map<String, List<String>> dbMetaInfo)
    {
        for (int row = getIdFieldRow(); row < getRowCount(); row++)
        {
            if (getCellCount(row) < 2)
            {
                continue;
            }
            Widget w = getWidget(row, 1);
            if ((w instanceof DbMetaInfoAware))
                ((DbMetaInfoAware) w).updateDbMetaInfo(dbContext, dbMetaInfo);
        }
    }

    public void commit()
    {
        EntityBaseDbMap result = (EntityBaseDbMap) get();

        int row = 1;
        result.setDbContext(MapUiUtil.getDbContext(getWidget(row, 1)));
        row++;

        result.setIdAccess(MapUiUtil.getAccess(getWidget(row, 1)));
        row++;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.mapping.MapUiEntityBaseEditor
 * JD-Core Version: 0.6.0
 */