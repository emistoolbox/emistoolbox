package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.ui.analysis.IndicatorConfigurationEditor;
import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.impl.IndicatorRatio;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.impl.MetaResultValueImpl;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MetaResultValueListEditor extends VerticalPanel implements HasValueChangeHandlers<List<MetaResultValue>>
{
    private static final int COL_DELETE = 0;
    private static final int COL_UPDOWN = 1;
    private static final int COL_INDICATOR = 2;
    private static final int COL_AGGREGATOR = 3;
    private static final int COL_TARGET = 4;
    private boolean indicatorOnly = false;
    private int listSize;
    private PushButton uiOkButton = new PushButton("OK");
    private PushButton uiTargetButton = new PushButton("OK");
    private PushButton uiSortButton = new PushButton("Sort");

    private ListBoxWithUserObjects<EmisIndicator> uiIndicators = new ListBoxWithUserObjects();
    private ListBoxWithUserObjects<String> uiAggregatorKeys = new ListBoxWithUserObjects();
    private TextBox uiTarget = new TextBox();
    private List<EmisIndicator> indicators;
    private List<MetaResultValue> values = new ArrayList();
    private List<Boolean> sortAscending = new ArrayList();

    private FlexTable uiValues = new FlexTable();
    private int currentRow = -1;

    public MetaResultValueListEditor(List<EmisIndicator> indicators, int listSize, boolean indicatorOnly) {
        this.listSize = listSize;
        this.indicatorOnly = indicatorOnly;
        this.indicators = indicators;

        this.uiIndicators.setWidth("225px");
        this.uiIndicators.add("", null);
        IndicatorConfigurationEditor.addAllIndicators(uiIndicators,  indicators);

        EmisUtils.init(this.uiOkButton, 60);
        this.uiOkButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                ValueChangeEvent.fire(MetaResultValueListEditor.this, MetaResultValueListEditor.this.get());
            }
        });
        EmisUtils.init(this.uiSortButton, 60);
        this.uiSortButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                MetaResultValueListEditor.this.showSortDialog();
            }
        });
        this.uiIndicators.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                MetaResultValueListEditor.this.onChangeIndicator();
            }
        });
        this.uiAggregatorKeys.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                MetaResultValueListEditor.this.onChangeAggregator();
            }
        });
        EmisUtils.init(this.uiTargetButton, 40);
        this.uiTargetButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                MetaResultValueListEditor.this.onChangeTarget();
            }
        });
        this.uiTarget.setWidth("50px");

        if (listSize != -1)
        {
            for (int i = 0; i < listSize; i++)
            {
                this.values.add(new MetaResultValueImpl());
                this.sortAscending.add(null);
            }

        }
        else
        {
            this.values.add(new MetaResultValueImpl());
            this.sortAscending.add(null);
        }

        add(this.uiValues);

        if ((listSize != 1) || (!indicatorOnly))
        {
            setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

            HorizontalPanel hp = null;
            if (listSize == 1)
            {
                add(this.uiOkButton);
            }
            else
            {
                hp = new HorizontalPanel();
                hp.setSpacing(2);
                hp.add(this.uiSortButton);
                hp.add(this.uiOkButton);
                add(hp);
            }
        }

        updateUi();
    }

    private void showSortDialog()
    {
        final PopupPanel popup = new PopupPanel();
        popup.setAutoHideEnabled(true);
        popup.setModal(true);

        SortEditor editor = new SortEditor(false);
        editor.set(new MetaResultValueSort(this.values, this.sortAscending));
        editor.addValueChangeHandler(new ValueChangeHandler<MetaResultValueSort>() {
            public void onValueChange(ValueChangeEvent<MetaResultValueSort> event)
            {
                if (event.getValue() != null)
                {
                    MetaResultValueSort result = (MetaResultValueSort) event.getValue();
                    MetaResultValueListEditor.this.set(result.getValues(), result.getSortAscending());
                }

                popup.hide();
            }
        });
        popup.setWidget(editor);
        popup.showRelativeTo(this.uiSortButton);
    }

    private void onChangeIndicator()
    {
        int row = this.currentRow;
        commit();
        if (row == -1)
        {
            return;
        }
        if (this.listSize == 1)
        {
            if (this.indicatorOnly)
                ValueChangeEvent.fire(this, get());
            else
            {
                editAggregator(row);
            }

        }
        else if ((this.listSize != -1) && (this.indicatorOnly) && (row + 1 == this.uiValues.getRowCount()))
        {
            ValueChangeEvent.fire(this, get());
        }
        else
            showNextIndicator(row);
    }

    private void showNextIndicator(int row)
    {
        if (row == -1)
        {
            return;
        }
        if ((this.listSize == -1) && (row + 1 == this.uiValues.getRowCount()))
        {
            this.values.add(new MetaResultValueImpl());
            this.sortAscending.add(null);
            updateUi();
        }

        if (row + 1 < this.uiValues.getRowCount())
            editIndicator(row + 1);
    }

    private void onChangeAggregator()
    {
        int row = this.currentRow;
        commit();
        MetaResultValue value = (MetaResultValue) this.values.get(row);
        if (isTargetValue(value))
            editAggregator(row);
        else
            showNextIndicator(row);
    }

    private void onChangeTarget()
    {
        int row = this.currentRow;
        commit();
        showNextIndicator(row);
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<MetaResultValue>> handler)
    {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    private void clearList()
    {
        while (this.uiValues.getRowCount() > 0)
            this.uiValues.removeRow(0);
        this.values.clear();
        this.sortAscending.clear();
    }

    public void updateUi(int suggestedEditRow)
    {
        if (this.currentRow != -1)
        {
            suggestedEditRow = this.currentRow;
        }
        suggestedEditRow = Math.min(this.values.size() - 1, suggestedEditRow);
        suggestedEditRow = Math.max(this.values.size() > 0 ? 0 : -1, suggestedEditRow);

        this.currentRow = suggestedEditRow;

        updateUi();
    }

    public void updateUi()
    {
        this.uiValues.removeAllRows();

        if (this.listSize == -1)
        {
            Iterator iter = this.values.iterator();
            while (iter.hasNext())
            {
                MetaResultValue value = (MetaResultValue) iter.next();
                if (value.getIndicator() == null)
                {
                    iter.remove();
                }
            }
        }
        while ((this.listSize != -1) && (this.values.size() < this.listSize))
        {
            this.values.add(new MetaResultValueImpl());
            this.sortAscending.add(null);
        }

        if (this.listSize == -1)
        {
            this.values.add(new MetaResultValueImpl());
            this.sortAscending.add(null);
        }

        int size = this.values.size();
        int row = 0;
        for (MetaResultValue value : this.values)
        {
            updateUi(row, value, size);
            if ((this.listSize > -1) && (this.listSize == row))
            {
                break;
            }
            row++;
        }
    }

    public void set(List<MetaResultValue> newValues, List<Boolean> newSortAscending)
    {
        clearList();

        if (newValues == null)
        {
            newValues = new ArrayList();
            newSortAscending = new ArrayList();
        }

        if (newSortAscending == null)
        {
            newSortAscending = new ArrayList();
            for (int i = 0; i < newValues.size(); i++)
            {
                newSortAscending.add(null);
            }
        }
        this.values = newValues;
        this.sortAscending = newSortAscending;

        updateUi(0);

        editIndicator(0);
    }

    public void set(List<MetaResultValue> newValues)
    {
        set(newValues, null);
    }

    private void updateUi(final int row, MetaResultValue value, int maxRows)
    {
        EmisIndicator indicator = value.getIndicator();
        checkedClearRow(row);

        this.uiValues.getRowFormatter().setVerticalAlign(row, HasVerticalAlignment.ALIGN_MIDDLE);

        if (this.listSize == 1)
        {
            return;
        }

        if (!isLastEmptyRow(row))
        {
            this.uiValues.setWidget(row, 0, getImage("css/del.png", new ClickHandler() {
                public void onClick(ClickEvent event)
                {
                    int lastRow = MetaResultValueListEditor.this.currentRow;

                    commit();
                    values.remove(MetaResultValueListEditor.this.values.get(row));
                    updateUi(row - 1);

                    if (MetaResultValueListEditor.this.currentRow >= row)
                        currentRow--;

                    if (lastRow != -1)
                        MetaResultValueListEditor.this.editIndicator(lastRow);
                }
            }));
        }

        Label l = new Label(indicator == null ? "(add)" : indicator.getName());
        l.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                MetaResultValueListEditor.this.editIndicator(row);
            }
        });
        this.uiValues.setWidget(row, 2, l);
        if ((indicator == null) || (this.indicatorOnly))
        {
            return;
        }
        if (!hasAggregators(indicator))
        {
            return;
        }
        l = new Label(value.getAggregatorKey() == null ? "(indicator)" : value.getName(false));
        l.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                MetaResultValueListEditor.this.editAggregator(row);
            }
        });
        this.uiValues.setWidget(row, 3, l);

        this.uiValues.setText(row, 4, "");
    }

    private boolean isTargetValue(MetaResultValue value)
    {
        if ((value == null) || (value.getAggregatorKey() == null))
        {
            return false;
        }
        return (value.getAggregatorKey().startsWith("?")) || (value.getAggregatorKey().startsWith("+"));
    }

    private boolean isValidTarget(String target)
    {
        if ((target == null) || (target.equals("")))
            return false;
        try
        {
            new Double(target);
            return true;
        }
        catch (NumberFormatException ex)
        {
        }
        return false;
    }

    private boolean isLastEmptyRow(int rowIndex)
    {
        return (this.values.size() == rowIndex + 1) && (((MetaResultValue) this.values.get(rowIndex)).getIndicator() == null);
    }

    private Image getImage(String url, ClickHandler handler)
    {
        Image result = new Image(url);
        result.addStyleName("pointer");
        result.addClickHandler(handler);

        return result;
    }

    private boolean hasAggregators(EmisIndicator indicator)
    {
        for (String key : indicator.getAggregatorNames())
        {
            EmisAggregatorDef aggr = indicator.getAggregator(key);
            if ((aggr.getName() != null) && (!aggr.getName().equals("")))
            {
                return true;
            }
        }
        return false;
    }

    private void editIndicator(int row)
    {
        if ((row == -1) || (row >= this.values.size()))
        {
            return;
        }
        commit();
        checkedClearCell(row, 2);
        this.uiValues.setWidget(row, 2, this.uiIndicators);

        MetaResultValue value = (MetaResultValue) this.values.get(row);
        if (value.getIndicator() == null)
            this.uiIndicators.setSelectedIndex(0);
        else
        {
            this.uiIndicators.setSelectedIndex(NamedUtil.findIndex(value.getIndicator(), this.indicators) + 1);
        }
        this.currentRow = row;
    }

    private void editAggregator(int row)
    {
        if ((row == -1) || (row >= this.values.size()))
        {
            return;
        }
        commit();
        checkedClearCell(row, 3);
        MetaResultValue value = (MetaResultValue) this.values.get(row);

        EmisIndicator indicator = value.getIndicator();
        if (indicator == null)
        {
            return;
        }
        this.uiAggregatorKeys.clear();
        this.uiAggregatorKeys.add("(indicator)", null);
        for (String key : indicator.getAggregatorNames())
        {
            EmisAggregatorDef aggr = indicator.getAggregator(key);
            if ((aggr.getName() == null) || (aggr.getName().equals("")))
            {
                continue;
            }
            this.uiAggregatorKeys.add(aggr.getName(), key);
        }

        if ((indicator instanceof IndicatorRatio))
        {
            for (String key : indicator.getAggregatorNames())
            {
                EmisAggregatorDef aggr = indicator.getAggregator(key);
                if ((aggr.getName() == null) || (aggr.getName().equals("")))
                {
                    continue;
                }
                if ((aggr.getMetaData() == null) || (!aggr.getMetaData().isPlanningResource()))
                    continue;
                this.uiAggregatorKeys.add("required " + aggr.getName(), "?" + key);
                this.uiAggregatorKeys.add("surplus " + aggr.getName(), "+" + key);
            }
        }

        if (this.uiAggregatorKeys.getItemCount() == 1)
        {
            showNextIndicator(row);
            return;
        }

        String currentKey = value.getAggregatorKey();
        for (int i = 0; i < this.uiAggregatorKeys.getItemCount(); i++)
        {
            if ((currentKey == null) || (!currentKey.equals(this.uiAggregatorKeys.getUserObject(i))))
                continue;
            this.uiAggregatorKeys.setSelectedIndex(i);
            break;
        }

        this.uiValues.setWidget(row, 3, this.uiAggregatorKeys);
        if ((currentKey != null) && ((currentKey.startsWith("?")) || (currentKey.startsWith("+"))))
        {
            if (value.getTarget() == null)
                this.uiTarget.setText("");
            else
            {
                this.uiTarget.setText("" + value.getTarget());
            }
            this.uiValues.setHTML(row, 4, currentKey.startsWith("?") ? "to reach" : "over");
            this.uiValues.setWidget(row, 5, this.uiTarget);
            this.uiValues.setWidget(row, 6, this.uiTargetButton);
        }
        else
        {
            checkedClearCell(row, 4);
            checkedClearCell(row, 5);
            checkedClearCell(row, 6);
        }

        this.currentRow = row;
    }

    private void editTarget(int row)
    {
        if ((row == -1) || (row >= this.values.size()))
        {
            return;
        }
        commit();
        checkedClearCell(row, 4);

        MetaResultValue value = (MetaResultValue) this.values.get(row);
        if (value.getTarget() != null)
            this.uiTarget.setText("" + value.getTarget());
        else
        {
            this.uiTarget.setText("");
        }
        this.uiValues.setText(row, 4, "to reach");
        this.uiValues.setWidget(row, 5, this.uiTarget);
        this.uiValues.setWidget(row, 6, this.uiTargetButton);

        this.currentRow = row;
    }

    private void commit()
    {
        if (this.currentRow == -1)
        {
            return;
        }
        MetaResultValue value = (MetaResultValue) this.values.get(this.currentRow);
        if ((getWidget(this.currentRow, 2) instanceof ListBoxWithUserObjects))
        {
            EmisIndicator prevIndicator = value.getIndicator();
            EmisIndicator newIndicator = (EmisIndicator) ((ListBoxWithUserObjects) getWidget(this.currentRow, 2)).getUserObject();
            value.setIndicator(newIndicator);
            if (!NamedUtil.sameName(newIndicator, prevIndicator))
            {
                value.setAggregatorKey(null);
                value.setAggregatorName(null);
            }
        }
        else if ((getWidget(this.currentRow, 3) instanceof ListBoxWithUserObjects))
        {
            String key = (String) ((ListBoxWithUserObjects) getWidget(this.currentRow, 3)).getUserObject();
            EmisIndicator indicator = value.getIndicator();
            if (indicator == null)
            {
                return;
            }
            if (key == null)
            {
                value.setAggregatorKey(null);
                value.setAggregatorName(null);
            }
            else
            {
                String aggrKey = key.startsWith("?") ? key.substring(1) : key;
                aggrKey = aggrKey.startsWith("+") ? aggrKey.substring(1) : aggrKey;

                EmisAggregatorDef aggr = indicator.getAggregator(aggrKey);
                if (aggr == null)
                {
                    return;
                }
                value.setAggregatorKey(key);
                value.setAggregatorName(aggr.getName());
            }
        }

        if ((getWidget(this.currentRow, 5) instanceof TextBox))
        {
            String target = ((TextBox) getWidget(this.currentRow, 5)).getText();
            if (isValidTarget(target))
                value.setTarget(new Double(target));
            else
            {
                value.setTarget(null);
            }
        }

        updateUi(this.currentRow, value, this.uiValues.getRowCount());
        this.currentRow = -1;
    }

    private Widget getWidget(int row, int col)
    {
        if ((row < this.uiValues.getRowCount()) && (col < this.uiValues.getCellCount(row)))
        {
            return this.uiValues.getWidget(row, col);
        }
        return null;
    }

    private void checkedClearCell(int row, int col)
    {
        if ((row < this.uiValues.getRowCount()) && (col < this.uiValues.getCellCount(row)))
            this.uiValues.clearCell(row, col);
    }

    private void checkedClearRow(int row)
    {
        if (row >= this.uiValues.getRowCount())
        {
            return;
        }
        for (int col = 0; col < this.uiValues.getCellCount(row); col++)
            this.uiValues.clearCell(row, col);
    }

    public List<Boolean> getSortAscending()
    {
        List result = new ArrayList();

        for (int i = 0; i < this.values.size(); i++)
        {
            if (((MetaResultValue) this.values.get(i)).getIndicator() == null)
            {
                continue;
            }
            result.add(this.sortAscending.get(i));
        }

        return result;
    }

    public List<MetaResultValue> get()
    {
        commit();

        List result = new ArrayList();
        for (MetaResultValue value : this.values)
        {
            if (value.getIndicator() == null)
            {
                continue;
            }
            result.add(value);
        }

        return result;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.MetaResultValueListEditor
 * JD-Core Version: 0.6.0
 */