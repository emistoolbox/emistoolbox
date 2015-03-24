package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.DbMapEditor;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowArrayAccess;
import com.emistoolbox.common.model.mapping.EmisEntityDbMap;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapUiEntityEditor extends MapUiEntityBaseEditor<EmisEntityDbMap>
{
    private EmisMetaDateEnum dateType;
    private ListBox uiFields = new ListBox();
    private int rowFields;

    public MapUiEntityEditor(DbMapEditor editor, EmisToolboxServiceAsync service, final MapUiTableOps tableOps) {
        super(service, editor, EmisUtils.init(new PushButton(Message.messageAdmin().mueeBtnDeleteMapping(), new ClickHandler() {
            public void onClick(ClickEvent event)
            { tableOps.del(); }
        }), 100));
    }

    public void set(EmisEntityDbMap map)
    {
        this.dateType = map.getDateEnum();
        if (this.dateType == null)
        {
            throw new IllegalArgumentException(Message.messageAdmin().mueeErrorNoDataTypeSet());
        }
        super.set(map);
    }

    protected void updateDataSource()
    {
        commit();
        EmisEntityDbMap result = (EmisEntityDbMap) get();

        for (int row = 2; row < getRowCount(); row++)
        {
            Widget w = getWidget(row, 1);
            if ((w instanceof MapUiWidgetBase))
                ((MapUiWidgetBase) w).setDbContext(result.getDbContext());
        }
    }

    private void deleteField(String name)
    {
        for (int row = 2; row < getRowCount(); row++)
        {
            Widget w = getWidget(row, 0);
            if ((!(w instanceof HTMLWithId)) || (!((HTMLWithId) w).getId().equals(name)))
                continue;
            removeRow(row);
            return;
        }
    }

    protected void updateUi()
    {
        updateFields();

        super.updateUi();

        int row = getRowCount() - 1;

        getFlexCellFormatter().setColSpan(row, 0, 1);
        getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
        setHTML(row, 0, Message.messageAdmin().mueeHtmlDateAccess() + ":");
        EmisToolbox.css(this, row, 0, "sectionBlue");

        setWidget(row, 1, updateDbMetaInfo(MapUiUtil.getMapUiAsWidget(this.dateType, ((EmisEntityDbMap) get()).getDateAccess(), getService(), getDataset())));
        EmisToolbox.css(this, row, 1, "adminEdit");
        row++;

        getFlexCellFormatter().setColSpan(row, 0, 2);
        setHTML(row, 0, "<hr>");
        row++;

        getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
        HorizontalPanel hp = new HorizontalPanel();
        hp.setStyleName("sectionBar");
        hp.setWidth("100%");
        hp.setSpacing(5);
        hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        hp.add(new Label(Message.messageAdmin().mueeLabelAddNewFieldMapping()));
        hp.add(this.uiFields);
        PushButton btn = new PushButton(Message.messageAdmin().mueeBtnAdd(), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                if (MapUiEntityEditor.this.uiFields.getSelectedIndex() == -1)
                    return;

                String field = MapUiEntityEditor.this.uiFields.getValue(MapUiEntityEditor.this.uiFields.getSelectedIndex());
                EmisMetaData emisField = (EmisMetaData) NamedUtil.find(field, ((EmisEntityDbMap) MapUiEntityEditor.this.get()).getEntity().getData()); 
                Widget tmp = MapUiUtil.getMapUiAsWidget(emisField, MapUiEntityEditor.this.getService(), getDataset()); 
                Widget w = MapUiEntityEditor.this.updateDbMetaInfo(tmp);
                int row = MapUiEntityEditor.this.getRowCount();
                MapUiEntityEditor.this.getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
                MapUiEntityEditor.this.setWidget(row, 0, MapUiEntityEditor.this.getFieldLabel(field));
                MapUiEntityEditor.this.setWidget(row, 1, w);

                MapUiEntityEditor.this.uiFields.removeItem(MapUiEntityEditor.this.uiFields.getSelectedIndex());
            }
        });
        hp.add(EmisUtils.init(btn, 60));

        setWidget(row, 1, hp);
        row++; 
        
        this.rowFields = row;

        for (Map.Entry<String, DbRowAccess> entry : ((EmisEntityDbMap) get()).getFieldAccess().entrySet())
        {
            getRowFormatter().setVerticalAlign(row, HasVerticalAlignment.ALIGN_TOP);
            setWidget(row, 0, getFieldLabel((String) entry.getKey()));
            EmisMetaData field = getField((String) entry.getKey());

            Widget w = null;
            if (field.getArrayDimensions() != null || field.getType() != EmisMetaData.EmisDataType.ENUM && field.getType() != EmisMetaData.EmisDataType.ENUM_SET)
                w = MapUiUtil.getMapUiAsWidget((DbRowAccess) entry.getValue(), entry.getValue() instanceof DbRowArrayAccess, field, getService(), getDataset());
            else
                w = MapUiUtil.getMapUiAsWidget((DbRowAccess) entry.getValue(), null, false, field, getService(), getDataset());

            setWidget(row, 1, updateDbMetaInfo(w));
            EmisToolbox.css(this, row, 1, "adminEdit");
            row++;
        }
    }

    private HTMLWithId getFieldLabel(final String field)
    {
        HTMLWithId html = new HTMLWithId("<div class='sectionBlue'>" + field + "</div><small>" + Message.messageAdmin().mueeHtmlDelete() + "</small>", field);
        html.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { MapUiEntityEditor.this.deleteField(field); }
        });
        return html;
    }

    private EmisMetaData getField(String key)
    {
        return (EmisMetaData) NamedUtil.find(key, ((EmisEntityDbMap) get()).getEntity().getData());
    }

    public void commit()
    {
        super.commit();
        EmisEntityDbMap result = (EmisEntityDbMap) get();

        result.setDateAccess(MapUiUtil.getDateAccess(getWidget(this.rowFields - 3, 1)));

        Map<String, DbRowAccess> fields = new HashMap<String, DbRowAccess>();
        for (int i = this.rowFields; i < getRowCount(); i++)
        {
            Widget w = getWidget(i, 0);
            if ((w instanceof HTMLWithId))
            {
                fields.put(((HTMLWithId) w).getId(), MapUiUtil.getAccess(getWidget(i, 1)));
            }
        }
        result.setFieldAccess(fields);
    }

    private void updateFields()
    {
        this.uiFields.clear();
        List<EmisMetaData> dataList = new ArrayList<EmisMetaData>();
        dataList.addAll(((EmisEntityDbMap) get()).getEntity().getData());

        Iterator<EmisMetaData> iter = dataList.iterator();
        while (iter.hasNext())
        {
            EmisMetaData data = (EmisMetaData) iter.next();
            if (data.getDateType().getName() != getDateType().getName())
            {
                iter.remove();
            }
        }
        List<String> fields = NamedUtil.getNames(dataList);
        for (int i = this.rowFields; i < getRowCount(); i++)
        {
            fields.remove(getText(i, 0));
        }
        Collections.sort(fields);
        for (String field : fields)
            this.uiFields.addItem(field);
    }

    public EmisMetaDateEnum getDateType()
    {
        return this.dateType;
    }
}
