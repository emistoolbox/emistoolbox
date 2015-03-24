package com.emistoolbox.client.ui.analysis;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageReport;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.google.gwt.user.client.ui.ListBox;

public class BoolFilterEditor extends ListBox
{
    private EmisMetaData field;

    public BoolFilterEditor(EmisMetaData field) {
        this.field = field;

        addItem("(" + Message.messageReport().bfeAll() + ")");
        addItem(Message.messageReport().bfeWith() + " " + field.getName());
        addItem(Message.messageReport().bfeWithout() + " " + field.getName());
    }

    public void set(Boolean filter)
    {
        if (filter == null)
            setSelectedIndex(0);
        else
            setSelectedIndex(filter.booleanValue() ? 1 : 2);
    }

    public Boolean get()
    {
        int index = getSelectedIndex();
        if ((index == -1) || (index == 0))
        {
            return null;
        }
        return Boolean.valueOf(index == 1);
    }

    public EmisMetaData getMetaData()
    {
        return this.field;
    }
}
