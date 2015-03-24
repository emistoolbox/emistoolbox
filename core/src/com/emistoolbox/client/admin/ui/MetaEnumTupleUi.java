package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.common.model.impl.MetaEnumTuple;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MetaEnumTupleUi extends FlexTable
{
    private EmisMetaEnumTuple tuple;
    private ListBox[] listboxes;
    private ListBox uiCount = new ListBox();
    private List<EmisMetaEnum> allEnums;
    private String titleHtml;

    public MetaEnumTupleUi(String titleHtml, boolean allowNoDimensions) {
        this.titleHtml = titleHtml;

        if (allowNoDimensions)
        {
            this.uiCount.addItem(Message.messageAdmin().metuNoArray(), "0");
        }
        this.uiCount.addItem(Message.messageAdmin().metuDimensional(1), "1");
        this.uiCount.addItem(Message.messageAdmin().metuDimensional(2), "2");
        this.uiCount.addItem(Message.messageAdmin().metuDimensional(3), "3");
        this.uiCount.addItem(Message.messageAdmin().metuDimensional(4), "4");
        this.uiCount.addItem(Message.messageAdmin().metuDimensional(5), "5");

        this.uiCount.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                MetaEnumTupleUi.this.onChangeDimension();
            }
        });
    }

    private void onChangeDimension()
    {
        EmisMetaEnum[] newEnums = new EmisMetaEnum[this.uiCount.getSelectedIndex()];
        EmisMetaEnum[] oldEnums = this.tuple.getEnums();

        for (int i = 0; i < Math.min(newEnums.length, oldEnums.length); i++)
        {
            newEnums[i] = oldEnums[i];
        }
        this.tuple.setEnums(newEnums);

        this.listboxes = new ListBox[newEnums.length];
        updateUi();
    }

    public void setAllEnums(List<EmisMetaEnum> allEnums)
    {
        this.allEnums = allEnums;
    }

    private void updateUi()
    {
        while (getRowCount() > 0)
        {
            removeRow(0);
        }
        if (this.titleHtml != null)
        {
            setHTML(0, 0, this.titleHtml);
        }
        setWidget(0, 1, this.uiCount);
        if (this.uiCount.getSelectedIndex() < 1)
        {
            return;
        }
        EmisMetaEnum[] values = this.tuple.getEnums();
        for (int lbIndex = 0; lbIndex < values.length; lbIndex++)
        {
            initListBox(lbIndex, values);

            getCellFormatter().setHorizontalAlignment(1 + lbIndex, 0, HasHorizontalAlignment.ALIGN_RIGHT);
            setHTML(1 + lbIndex, 0, "<b>" + (lbIndex + 1) + ":</b>");
            setWidget(1 + lbIndex, 1, this.listboxes[lbIndex]);
        }
    }

    private void initListBox(final int index, EmisMetaEnum[] values)
    {
        if (this.listboxes[index] == null)
        {
            this.listboxes[index] = new ListBox();
            this.listboxes[index].addChangeHandler(new ChangeHandler() {
                public void onChange(ChangeEvent event)
                {
                    EmisMetaEnum[] values = MetaEnumTupleUi.this.tuple.getEnums();
                    int selectedItem = MetaEnumTupleUi.this.listboxes[index].getSelectedIndex();
                    if (selectedItem < 1)
                        values[index] = null;
                    else
                    {
                        values[index] = NamedUtil.find(listboxes[index].getValue(selectedItem), allEnums);
                    }

                    tuple.setEnums(values);

                    for (int i = 0; i < MetaEnumTupleUi.this.listboxes.length; i++)
                        MetaEnumTupleUi.this.initListBox(i, values);
                }
            });
        }
        else
        {
            this.listboxes[index].clear();
        }
        this.listboxes[index].addItem("");
        for (EmisMetaEnum item : getChoices(values, index))
        {
            this.listboxes[index].addItem(item.getName());
            if ((values[index] != null) && (item.getName().equals(values[index].getName())))
                this.listboxes[index].setSelectedIndex(this.listboxes[index].getItemCount() - 1);
        }
    }

    private List<EmisMetaEnum> getChoices(EmisMetaEnum[] values, int selectedIndex)
    {
        List result = new ArrayList();
        result.addAll(this.allEnums);

        List enums = new ArrayList();
        enums.addAll(Arrays.asList(values));
        enums.remove(selectedIndex);
        result.removeAll(enums);

        return result;
    }

    public void set(EmisMetaEnumTuple tuple)
    {
        if (tuple == null)
        {
            tuple = new MetaEnumTuple();
            tuple.setEnums(new EmisMetaEnum[0]);
        }

        this.tuple = tuple;
        this.uiCount.setSelectedIndex(tuple.getDimensions());
        onChangeDimension();
    }

    public EmisMetaEnumTuple get()
    {
        if (this.tuple.getEnums().length == 0)
        {
            return null;
        }
        return this.tuple;
    }
}
