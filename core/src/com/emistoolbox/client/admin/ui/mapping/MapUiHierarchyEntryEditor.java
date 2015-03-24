package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.DbMapEditor;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMapEntry;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;
import java.util.Map;

public class MapUiHierarchyEntryEditor extends MapUiEditorBase<EmisHierarchyDbMapEntry> implements DbMetaInfoAware
{
    private DbMapEditor editor;
    private MapUiDbContextEditor dbContextEditor;

    public MapUiHierarchyEntryEditor(DbMapEditor editor, EmisToolboxServiceAsync service) {
        super(service, editor.getDataset());

        this.editor = editor;
        getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
        setHTML(0, 0, Message.messageAdmin().muheeHtmlDataAccess());
        EmisToolbox.css(this, 0, 0, "sectionBlue");

        getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
        setHTML(1, 0, Message.messageAdmin().muheeHtmlDateAccess());
        EmisToolbox.css(this, 1, 0, "sectionBlue");

        getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
        getCellFormatter().setVerticalAlignment(3, 0, HasVerticalAlignment.ALIGN_TOP);
    }

    public void updateUi()
    {
        EmisHierarchyDbMapEntry entry = (EmisHierarchyDbMapEntry) get();
        this.dbContextEditor = MapUiUtil.getMapUiAsWidget(entry.getDbContext(), entry.getDateAccess() != null ? entry.getDateAccess().getDateType() : entry.getDateType(), this.editor);
        this.dbContextEditor.addValueChangeHandler(new ValueChangeHandler<DbContext>() {
            public void onValueChange(ValueChangeEvent<DbContext> event)
            { MapUiHierarchyEntryEditor.this.updateDbMetaInfo(); }
        });

        setWidget(0, 1, this.dbContextEditor);
        EmisToolbox.css(this, 0, 1, "adminEdit"); 

        setWidget(1, 1, updateDbMetaInfo(MapUiUtil.getMapUiAsWidget(entry.getDateType(), entry.getDateAccess(), getService(), getDataset())));
        EmisToolbox.css(this, 1, 1, "adminEdit");

        setHTML(2, 0, Message.messageAdmin().muheeHtmlIdFor(entry.getParentEntity().getName()));
        EmisToolbox.css(this, 2, 0, "sectionBlue");

        setWidget(2, 1, updateDbMetaInfo(MapUiUtil.getMapUiAsWidget(entry.getParentAccess(), getService(), getDataset())));
        EmisToolbox.css(this, 2, 1, "adminEdit");

        setHTML(3, 0, Message.messageAdmin().muheeHtmlIdFor(entry.getChildEntity().getName()));
        EmisToolbox.css(this, 3, 0, "sectionBlue");

        setWidget(3, 1, updateDbMetaInfo(MapUiUtil.getMapUiAsWidget(entry.getChildAccess(), getService(), getDataset())));
        EmisToolbox.css(this, 3, 1, "adminEdit");
    }

    public void commit()
    {
        EmisHierarchyDbMapEntry entry = (EmisHierarchyDbMapEntry) get();
        entry.setDbContext(MapUiUtil.getDbContext(getWidget(0, 1)));
        entry.setDateAccess(MapUiUtil.getDateAccess(getWidget(1, 1)));
        entry.setParentAccess(MapUiUtil.getAccess(getWidget(2, 1)));
        entry.setChildAccess(MapUiUtil.getAccess(getWidget(3, 1)));
    }

    public void set(EmisHierarchyDbMapEntry data)
    {
        super.set(data);
        updateUi();
    }

    public void updateDbMetaInfo()
    {
        DbContext dbContext = this.dbContextEditor.get();
        updateDbMetaInfo(dbContext, this.editor.getDbMetaInfo(dbContext == null ? null : dbContext.getDataSource()));
    }

    public void updateDbMetaInfo(DbContext context, Map<String, List<String>> dbMetaInfo)
    {
        this.dbContextEditor.updateDbMetaInfo();
        updateDbMetaInfo(getWidget(1, 1), context, dbMetaInfo);
        updateDbMetaInfo(getWidget(2, 1), context, dbMetaInfo);
        updateDbMetaInfo(getWidget(3, 1), context, dbMetaInfo);
    }

    private Widget updateDbMetaInfo(Widget w)
    {
        DbContext dbContext = this.dbContextEditor.get();
        if ((w instanceof DbMetaInfoAware))
        {
            ((DbMetaInfoAware) w).updateDbMetaInfo(dbContext, this.editor.getDbMetaInfo(dbContext == null ? null : dbContext.getDataSource()));
        }
        return w;
    }

    private void updateDbMetaInfo(Widget w, DbContext context, Map<String, List<String>> dbMetaInfo)
    {
        if ((w instanceof DbMetaInfoAware))
            ((DbMetaInfoAware) w).updateDbMetaInfo(context, dbMetaInfo);
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.client.admin.ui.mapping.MapUiHierarchyEntryEditor JD-Core
 * Version: 0.6.0
 */