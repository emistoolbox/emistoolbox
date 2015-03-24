package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class EnumMapEditor extends FlexTable
{
    private Map<String, TextBox> uiTextBoxes = new HashMap();

    public EnumMapEditor(EmisMetaEnum enumType) {
        setStyleName("enumMapping");
        getFlexCellFormatter().setColSpan(0, 0, 2);
        setHTML(0, 0, Message.messageAdmin().emeInfoUseCommaForMultipleValues());
        int row = 1;
        for (String value : enumType.getValues())
        {
            setText(row, 0, value);
            this.uiTextBoxes.put(value, new TextBox());
            setWidget(row, 1, (Widget) this.uiTextBoxes.get(value));
            row++;
        }
    }

    public void set(Map<String, String> mapping)
    {
        for (TextBox tb : this.uiTextBoxes.values())
        {
            tb.setText("");
        }
        if (mapping == null)
        {
            return;
        }
        for (Map.Entry entry : mapping.entrySet())
        {
            TextBox tb = (TextBox) this.uiTextBoxes.get(entry.getValue());
            if (tb == null)
            {
                continue;
            }
            String text = tb.getText();
            if ((text == null) || (text.equals("")))
                tb.setText((String) entry.getKey());
            else
                tb.setText(text + "," + (String) entry.getKey());
        }
    }

    public Map<String, String> get()
    {
        Map result = new HashMap();
        for (Map.Entry entry : this.uiTextBoxes.entrySet())
        {
            String text = ((TextBox) entry.getValue()).getText();
            if (text == null)
            {
                continue;
            }
            String[] values = text.split(",");
            for (String value : values)
            {
                value = value.trim();
                if (value.equals(""))
                {
                    continue;
                }
                result.put(value, entry.getKey());
            }
        }

        return result;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.mapping.EnumMapEditor JD-Core
 * Version: 0.6.0
 */