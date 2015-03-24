package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowDateAccess;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;
import java.util.Map;

public class MapUiDate extends MapUiWidgetBase
{
    private DbRowDateAccess access;

    public MapUiDate(EmisToolboxServiceAsync service, String dataset) 
    { super(service, dataset); }

    public DbRowAccess get()
    {
        DbRowAccess[] accesses = new DbRowAccess[this.access.getDateType().getDimensions()];
        for (int row = 0; (row < getRowCount()) && (row < accesses.length); row++)
        {
            accesses[row] = MapUiUtil.getAccess(getWidget(row, 1));
        }
        this.access.setAccesses(accesses);

        return this.access;
    }

    public void getValuePreview(AsyncCallback<List<String>> callback)
    {
    }

    public void set(DbRowAccess newAccess)
    {
        if (!(newAccess instanceof DbRowDateAccess))
        {
            return;
        }
        this.access = ((DbRowDateAccess) newAccess);
        clear(true);

        EmisMetaEnum[] enums = this.access.getDateType().getEnums();
        DbRowAccess[] accesses = this.access.getAccesses();
        for (int row = 0; row < accesses.length; row++)
        {
            getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
            setHTML(row, 0, enums[row].getName());
            EmisToolbox.css(this, row, 0, "section"); 
            
            MapUiWidget mapui = MapUiUtil.getMapUi(accesses[row], enums[row], false, null, getService(), getDataset());
            mapui.setConstraint(new MapUiEnumConstraint(enums[row]));
            mapui.set(accesses[row]);

            setWidget(row, 1, (Widget) mapui);
        }
    }

    public void updateDbMetaInfo()
    {
        for (int row = 0; row < getRowCount(); row++)
        {
            Widget w = getWidget(row, 1);
            if ((w instanceof DbMetaInfoAware))
                ((DbMetaInfoAware) w).updateDbMetaInfo();
        }
    }

    public void updateDbMetaInfo(DbContext dbContext, Map<String, List<String>> dbMetaInfo)
    {
        for (int row = 0; row < getRowCount(); row++)
        {
            Widget w = getWidget(row, 1);
            if ((w instanceof DbMetaInfoAware))
                ((DbMetaInfoAware) w).updateDbMetaInfo(dbContext, dbMetaInfo);
        }
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.mapping.MapUiDate JD-Core
 * Version: 0.6.0
 */