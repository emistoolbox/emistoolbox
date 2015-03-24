package com.emistoolbox.client.ui.analysis;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.impl.EnumSetImpl;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class EnumSetEditor extends FlexTable implements EmisEditor<EmisEnumSet>
{
    private EmisMetaData field;
    private EmisEnumSet enumSet;
    private CheckBox[] uiChecks;

    public EnumSetEditor()
    { init(null); }
    
    public EnumSetEditor(EmisMetaEnum enumType) 
    {
    	EmisEnumSet values = new EnumSetImpl(); 
    	values.setEnum(enumType);
    	values.setAll();
    	
    	init(values); 
    }
    
    private void init(EmisEnumSet initValue) 
    {
        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(1);
        HTML b = new HTML(" <small>" + Message.messageAdmin().eseHtmlSelectAll() + "</small> ");
        b.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                for (int i = 0; i < EnumSetEditor.this.uiChecks.length; i++)
                    EnumSetEditor.this.uiChecks[i].setValue(Boolean.valueOf(true));
            }
        });
        hp.add(b);

        b = new HTML(" <small>" + Message.messageAdmin().eseHtmlClearAll() + "</small> ");
        b.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                for (int i = 0; i < EnumSetEditor.this.uiChecks.length; i++)
                    EnumSetEditor.this.uiChecks[i].setValue(Boolean.valueOf(false));
            }
        });
        hp.add(b);

        getFlexCellFormatter().setColSpan(0, 0, 6);
        setWidget(0, 0, hp);
        
        if (initValue != null)
        	set(initValue); 
    }

    public void setMetaData(EmisMetaData field)
    {
        this.field = field;
    }

    public void commit()
    {
        this.enumSet.clear();
        for (byte i = 0; i < this.uiChecks.length; i = (byte) (i + 1))
        {
            if (this.uiChecks[i].getValue().booleanValue())
                this.enumSet.addValue(i);
        }
    }

    public EmisEnumSet get()
    {
        commit();
        return this.enumSet;
    }

    public void updateUi()
    {
        EmisMetaEnum enumType = this.enumSet.getEnum();
        String[] values = enumType.getValues();

        while (getRowCount() > 1)
        {
            removeRow(1);
        }
        int row = 1;
        for (byte i = 0; i < values.length; i = (byte) (i + 1))
        {
            setWidget(row, 2 * (i % 3), this.uiChecks[i]);
            setText(row, 2 * (i % 3) + 1, values[i]);
            if (i % 3 == 2)
                row++;
        }
    }

    public void set(EmisEnumSet data)
    {
        this.enumSet = data;
        this.uiChecks = new CheckBox[this.enumSet.size()];
        for (byte i = 0; i < this.uiChecks.length; i = (byte) (i + 1))
        {
            this.uiChecks[i] = new CheckBox();
            this.uiChecks[i].setValue(Boolean.valueOf(this.enumSet.hasValue(i)));
        }

        updateUi();
    }

    public EmisMetaData getMetaData()
    {
        return this.field;
    }
}
