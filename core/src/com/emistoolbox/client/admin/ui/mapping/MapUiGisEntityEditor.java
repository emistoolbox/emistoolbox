package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.client.admin.DbMapEditor;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.common.model.mapping.GisEntityDbMap;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEntity.EmisGisType;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class MapUiGisEntityEditor extends MapUiEntityBaseEditor<GisEntityDbMap>
{
    private ListBox uiCoordType = new ListBox();
    private int gisRow = 0;

    private MapUiWidget uiPrimary = null;
    private MapUiWidget uiSecondary = null;

    public MapUiGisEntityEditor(DbMapEditor editor, EmisToolboxServiceAsync service) {
        super(service, editor, null);

        this.uiCoordType.addItem(Message.messageAdmin().gisMapSingleLongLatField());
        this.uiCoordType.addItem(Message.messageAdmin().gisMapSeperateLongLatFields());
        this.uiCoordType.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                if (MapUiGisEntityEditor.this.uiCoordType.getSelectedIndex() == 0)
                {
                    MapUiGisEntityEditor.this.setHTML(MapUiGisEntityEditor.this.gisRow + 1, 0, Message.messageAdmin().gisLongLat() + ":");
                    EmisToolbox.css(MapUiGisEntityEditor.this, MapUiGisEntityEditor.this.gisRow + 1, 0, "sectionBlue");

                    MapUiGisEntityEditor.this.setHTML(MapUiGisEntityEditor.this.gisRow + 2, 0, "");
                    EmisToolbox.css(MapUiGisEntityEditor.this, MapUiGisEntityEditor.this.gisRow + 2, 0, "sectionBlue");
                    ((Widget) MapUiGisEntityEditor.this.uiSecondary).setVisible(false);
                }
                else
                {
                    MapUiGisEntityEditor.this.setHTML(MapUiGisEntityEditor.this.gisRow + 1, 0, Message.messageAdmin().gisLongitude() + ":");
                    EmisToolbox.css(MapUiGisEntityEditor.this, MapUiGisEntityEditor.this.gisRow + 1, 0, "sectionBlue");
                    MapUiGisEntityEditor.this.setHTML(MapUiGisEntityEditor.this.gisRow + 2, 0, Message.messageAdmin().gisLatitude() + ":");
                    EmisToolbox.css(MapUiGisEntityEditor.this, MapUiGisEntityEditor.this.gisRow + 2, 0, "sectionBlue");
                    ((Widget) MapUiGisEntityEditor.this.uiSecondary).setVisible(true);
                }
            }
        });
    }

    public void updateUi()
    {
        super.updateUi();

        this.gisRow = getRowCount();

        GisEntityDbMap item = (GisEntityDbMap) get();
        this.uiPrimary = MapUiUtil.getMapUi(item.getPrimaryAccess(), false, null, getService(), getDataset());
        this.uiSecondary = MapUiUtil.getMapUi(item.getSecondaryAccess(), false, null, getService(), getDataset());

        if (item.getEntity().getGisType() == EmisMetaEntity.EmisGisType.POLYGON)
        {
            setHTML(this.gisRow + 1, 0, Message.messageAdmin().gisLongLatList() + ":");
            EmisToolbox.css(this, gisRow + 1, 0, "sectionBlue");

            setWidget(this.gisRow + 1, 1, (Widget) this.uiPrimary);
        }
        else
        {
            setWidget(this.gisRow, 1, this.uiCoordType);

            setWidget(this.gisRow + 1, 1, (Widget) this.uiPrimary);
            setWidget(this.gisRow + 2, 1, (Widget) this.uiSecondary);

            if (item.getSecondaryAccess() == null)
            {
                this.uiCoordType.setSelectedIndex(0);
                setHTML(this.gisRow + 1, 0, Message.messageAdmin().gisLongLat() + ":");
                EmisToolbox.css(this, gisRow + 1, 0, "sectionBlue");
                ((Widget) this.uiSecondary).setVisible(false);
            }
            else
            {
                this.uiCoordType.setSelectedIndex(1);
                setHTML(this.gisRow + 1, 0, Message.messageAdmin().gisLongitude() + ":");
                EmisToolbox.css(this, gisRow + 1, 0, "sectionBlue");
                setHTML(this.gisRow + 2, 0, Message.messageAdmin().gisLatitude() + ":");
                EmisToolbox.css(this, gisRow + 2, 0, "sectionBlue");
                ((Widget) this.uiSecondary).setVisible(true);
            }
        }

        getCellFormatter().setVerticalAlignment(this.gisRow + 1, 0, HasVerticalAlignment.ALIGN_TOP);
        getCellFormatter().setVerticalAlignment(this.gisRow + 2, 0, HasVerticalAlignment.ALIGN_TOP);
    }

    public void commit()
    {
        super.commit();

        GisEntityDbMap result = (GisEntityDbMap) get();
        result.setPrimaryAccess(MapUiUtil.getAccess(getWidget(this.gisRow + 1, 1)));
        if ((result.getEntity().getGisType() != EmisMetaEntity.EmisGisType.POLYGON) && (this.uiCoordType.getSelectedIndex() > 0))
            result.setSecondaryAccess(MapUiUtil.getAccess(getWidget(this.gisRow + 2, 1)));
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.mapping.MapUiGisEntityEditor
 * JD-Core Version: 0.6.0
 */