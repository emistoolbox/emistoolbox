package com.emistoolbox.client.admin.ui;

import com.emistoolbox.common.results.MetaResultValue;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RadioButton;
import java.util.List;

public class SortEditor extends FlexTable implements HasValueChangeHandlers<MetaResultValueSort>
{
    private List<MetaResultValue> values;
    private PushButton uiOkButton = new PushButton("OK");
    private PushButton uiCancelButton = new PushButton("Cancel");
    private boolean withOrderUi;

    public SortEditor(boolean withOrderUi) {
        setCellSpacing(2);
        EmisUtils.init(this.uiOkButton, 60);
        EmisUtils.init(this.uiCancelButton, 60);

        this.uiOkButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                ValueChangeEvent.fire(SortEditor.this, SortEditor.this.get());
            }
        });
        this.uiCancelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                ValueChangeEvent.fire(SortEditor.this, null);
            }
        });
        this.withOrderUi = withOrderUi;
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<MetaResultValueSort> handler)
    {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    private void format(int row, int col)
    {
        getFlexCellFormatter().setRowSpan(row, col, 2);
        getFlexCellFormatter().setVerticalAlignment(row, col, HasVerticalAlignment.ALIGN_MIDDLE);
        getRowFormatter().addStyleName(row, "height20");  
    }

    public void set(MetaResultValueSort sort)
    {
        clear();
        this.values = sort.getValues();

        for (int i = 0; i < this.values.size(); i++)
        {
            format(i * 2, 0);
            setHTML(i * 2, 0, "<b>" + (i == 0 ? "Sort by:" : "Then by:") + "</b>");
            format(i * 2, 1);
            setWidget(i * 2, 1, getValueListBox(i));

            if (!this.withOrderUi)
            {
                continue;
            }
            RadioButton radio = new RadioButton("sort" + i, "Ascending");
            Boolean ascending = sort.getSortAscending(i);
            radio.setValue(Boolean.valueOf((ascending == null) || (ascending.booleanValue())));
            setWidget(i * 2, 2, radio);
            radio = new RadioButton("sort" + i, "Descending");
            radio.setValue(Boolean.valueOf((ascending != null) && (!ascending.booleanValue())));
            setWidget(i * 2 + 1, 2, radio);
        }

        HorizontalPanel buttons = new HorizontalPanel();
        buttons.add(this.uiOkButton);
        buttons.add(this.uiCancelButton);

        getFlexCellFormatter().setColSpan(this.values.size() * 2, 0, 3);
        getFlexCellFormatter().setHorizontalAlignment(this.values.size() * 2, 0, HasHorizontalAlignment.ALIGN_RIGHT);

        setWidget(this.values.size() * 2, 0, buttons);
    }

    private ListBox getValueListBox(int pos)
    {
        ListBox result = new ListBox();
        result.addItem("");
        for (MetaResultValue value : this.values)
        {
            result.addItem(value.getName(true));
        }
        result.setSelectedIndex(pos + 1);
        result.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                SortEditor.this.verifyAll();
            }
        });
        return result;
    }

    private void verifyAll()
    {
        boolean[] used = new boolean[this.values.size()];
        for (int i = 0; i < used.length; i++)
        {
            int index = getSelectedIndex(i);
            if ((index == -1) || (used[index]))
            {
                setSelectedIndex(i, -1);
            }
            else
            {
                used[index] = true;
            }
        }
    }

    private boolean getSortAscending(int index)
    {
        if (!this.withOrderUi)
        {
            return false;
        }
        RadioButton radio = (RadioButton) getWidget(index * 2, 2);
        return radio.getValue().booleanValue();
    }

    public MetaResultValueSort get()
    {
        MetaResultValueSort result = new MetaResultValueSort();

        boolean[] used = new boolean[this.values.size()];
        for (int i = 0; i < used.length; i++)
        {
            int index = getSelectedIndex(i);
            if ((index == -1) || (used[index]))
            {
                continue;
            }
            result.add((MetaResultValue) this.values.get(index), Boolean.valueOf(getSortAscending(index)));
            used[index] = true;
        }

        for (int i = 0; i < used.length; i++)
        {
            if (!used[i])
            {
                result.add((MetaResultValue) this.values.get(i), null);
            }
        }
        return result;
    }

    private void setSelectedIndex(int pos, int value)
    {
        ListBox lb = (ListBox) getWidget(pos * 2, 1);
        lb.setSelectedIndex(value + 1);
    }

    private int getSelectedIndex(int pos)
    {
        ListBox lb = (ListBox) getWidget(pos * 2, 1);
        int result = lb.getSelectedIndex();
        if (result > -1)
        {
            result--;
        }
        return result;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.SortEditor JD-Core Version:
 * 0.6.0
 */