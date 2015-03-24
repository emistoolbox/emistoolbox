package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.Message;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowArrayAccess;
import com.emistoolbox.common.model.mapping.DbRowMultipleAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowAccessMultipleAccessIndex;
import com.emistoolbox.common.model.mapping.impl.DbRowMultipleAccessImpl;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import java.util.Map;

public class MapUiArray extends MapUiWidgetBase
{
    private DbRowArrayAccess access;
    private EmisMetaEnum[] enums;
    private DbRowMultipleAccess columnAccess;
    private int columnAccessIndex;
    private int rowValueAccess;
    private int rowEnumAccess;
    private ListBox uiColumnChoice = new ListBox();

    private Map<String, List<String>> currentDbMetaInfo = null;

    public MapUiArray(EmisToolboxServiceAsync service, String dataset) 
    {
        super(service, dataset);

        getRowFormatter().setVerticalAlign(1, HasVerticalAlignment.ALIGN_TOP);
        setHTML(1, 0, Message.messageAdmin().mapuiaPromptMoreThanOneValueInRow());
        setWidget(1, 1, this.uiColumnChoice);

        getFlexCellFormatter().setColSpan(2, 0, 2);
        setHTML(2, 0, "<hr>");

        getRowFormatter().setVerticalAlign(3, HasVerticalAlignment.ALIGN_TOP);
        this.rowValueAccess = 3;
        setHTML(this.rowValueAccess, 0, Message.messageAdmin().mapuiaRowValueAccess());

        getFlexCellFormatter().setColSpan(4, 0, 2);
        setHTML(4, 0, "<hr>");

        getRowFormatter().setVerticalAlign(5, HasVerticalAlignment.ALIGN_TOP);
        this.rowEnumAccess = 5;
        setHTML(this.rowEnumAccess, 0, Message.messageAdmin().mapuiaRowEnumAccess());
    }

    public DbRowAccess get()
    {
        commit();
        return this.access;
    }

    public void setMetaContext(EmisMetaData field)
    {
        super.setMetaContext(field);

        if (field == null)
        {
            throw new IllegalArgumentException(Message.messageAdmin().mapuiaErrorArrayNeedsToSpecifyEmisMetaDataType());
        }
        if (this.access == null)
            this.access = MapUiUtil.createArrayAccess(getMetaContext()); 

        this.uiColumnChoice.clear();
        this.uiColumnChoice.addItem(Message.messageAdmin().mapuiaInfoNoEachRowHasOnlyOneValue(), "-1");
        this.enums = field.getArrayDimensions().getEnums();
        this.columnAccessIndex = -1;
        this.columnAccess = null;

        for (int i = 0; i < this.enums.length; i++)
        {
            this.uiColumnChoice.addItem(Message.messageAdmin().mapuiaInfoYesFor(this.enums[i].getName()), "" + i);
        }
        this.uiColumnChoice.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                MapUiArray.this.commit();

                columnAccessIndex = -1;
                if (MapUiArray.this.uiColumnChoice.getSelectedIndex() != -1)
                {
                    columnAccessIndex = Integer.parseInt(MapUiArray.this.uiColumnChoice.getValue(MapUiArray.this.uiColumnChoice.getSelectedIndex()));
                    if (MapUiArray.this.columnAccessIndex != -1)
                    {
                        columnAccess = createColumnAccess(MapUiArray.this.enums[MapUiArray.this.columnAccessIndex]);
                    }
                }
                MapUiArray.this.updateUi();
            }
        });
        updateUi();
    }

    private void updateUi()
    {
        if (this.columnAccessIndex == -1)
            setWidget(this.rowValueAccess, 1, MapUiUtil.getMapUiAsWidget(this.access == null ? null : this.access.getValueAccess(), getMetaContext(), getService(), getDataset()));
        else
        {
            setWidget(this.rowValueAccess, 1, getColumnAccess());
        }
        setWidget(this.rowEnumAccess, 1, getEnumAccess());

        updateDbMetaInfo(getDbContext(), this.currentDbMetaInfo);
    }

    private void commit()
    {
        if (this.columnAccess != null)
        {
            commit(this.columnAccess);
        }
        DbRowAccess[] indexAccesses = new DbRowAccess[this.enums.length];
        for (int i = 0; i < indexAccesses.length; i++)
        {
            if (i == this.columnAccessIndex)
                indexAccesses[i] = this.columnAccess.getIndexAccess();
            else
            {
                indexAccesses[i] = getEnumAccess(i);
            }
        }
        this.access.setIndexAccess(indexAccesses);

        if (this.columnAccess == null)
            this.access.setValueAccess(MapUiUtil.getAccess(getWidget(this.rowValueAccess, 1)));
        else
            this.access.setValueAccess(this.columnAccess);
    }

    private void commit(DbRowMultipleAccess multiAccess)
    {
        Grid g = (Grid) getWidget(this.rowValueAccess, 1);

        DbRowAccess[] accesses = multiAccess.getAccesses();
        for (int i = 0; i < accesses.length; i++)
        {
            accesses[i] = MapUiUtil.getAccess(g.getWidget(i, 0));
        }

        multiAccess.setAccesses(accesses);
    }

    public void set(DbRowAccess newAccess)
    {
        if (!(newAccess instanceof DbRowArrayAccess))
        {
            return;
        }
        this.access = ((DbRowArrayAccess) newAccess);

        this.columnAccessIndex = -1;

        DbRowAccess[] accesses = this.access.getIndexAccess();
        for (int i = 0; i < accesses.length; i++)
        {
            if (!(accesses[i] instanceof DbRowAccessMultipleAccessIndex))
                continue;
            this.columnAccessIndex = i;
            this.columnAccess = ((DbRowMultipleAccess) this.access.getValueAccess());
            this.uiColumnChoice.setSelectedIndex(i + 1);
        }

        updateUi();
    }

    private DbRowMultipleAccess createColumnAccess(EmisMetaEnum anEnum)
    {
        DbRowMultipleAccess result = new DbRowMultipleAccessImpl();
        result.setIndexes(anEnum.getValues());
        return result;
    }

    private Grid getColumnAccess()
    {
        String[] indexes = this.columnAccess.getIndexes();
        DbRowAccess[] rowAccess = this.columnAccess.getAccesses();

        Grid result = new Grid(indexes.length, 3);
        for (int i = 0; i < indexes.length; i++)
        {
            result.getRowFormatter().setVerticalAlign(i, HasVerticalAlignment.ALIGN_TOP);
            result.setWidget(i, 0, MapUiUtil.getMapUiAsWidget(rowAccess[i], getMetaContext(), getService(), getDataset()));
            result.setText(i, 1, " for ");
            result.setText(i, 2, indexes[i]);
        }

        return result;
    }

    private DbRowAccess getEnumAccess(int index)
    {
        Grid g = (Grid) getWidget(this.rowEnumAccess, 1);

        if (g.getCellCount(index) < 2)
            return null;
        MapUiWidget w = (MapUiWidget) g.getWidget(index, 1);
        return w.get();
    }

    private Grid getEnumAccess()
    {
        DbRowAccess[] indexAccess = this.access.getIndexAccess();

        Grid result = new Grid(indexAccess.length, 2);
        for (int i = 0; i < this.enums.length; i++)
        {
            result.getRowFormatter().setVerticalAlign(i, HasVerticalAlignment.ALIGN_TOP);
            if (i == this.columnAccessIndex)
                continue;

            result.setHTML(i, 0, "<b class='light'>" + this.enums[i].getName() + "</b>");
            result.setWidget(i, 1, MapUiUtil.getMapUiAsWidget(indexAccess[i], this.enums[i], false, null, getService(), getDataset()));
        }

        return result;
    }

    public void getValuePreview(AsyncCallback<List<String>> callback)
    { callback.onSuccess(null); }

    public void updateDbMetaInfo()
    {
    }

    public void updateDbMetaInfo(DbContext dbContext, Map<String, List<String>> dbMetaInfo)
    {
        setDbContext(dbContext);
        this.currentDbMetaInfo = dbMetaInfo;
        updateDbMetaInfo(dbContext, dbMetaInfo, getWidget(this.rowValueAccess, 1), 0);
        updateDbMetaInfo(dbContext, dbMetaInfo, getWidget(this.rowEnumAccess, 1), 1);
    }

    private void updateDbMetaInfo(DbContext dbContext, Map<String, List<String>> dbMetaInfo, Widget w, int col)
    {
        if ((w instanceof DbMetaInfoAware))
        {
            ((DbMetaInfoAware) w).updateDbMetaInfo(dbContext, dbMetaInfo);
        }
        else if (((w instanceof Grid)) && (col != -1))
        {
            Grid g = (Grid) w;
            for (int row = 0; row < g.getRowCount(); row++)
                updateDbMetaInfo(dbContext, dbMetaInfo, g.getWidget(row, col), col);
        }
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.mapping.MapUiArray JD-Core
 * Version: 0.6.0
 */