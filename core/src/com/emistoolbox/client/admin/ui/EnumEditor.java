package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.Message;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class EnumEditor extends FlexTable
{
    private EmisMetaEnum metaEnum;
    private ListBox uiValues = new ListBox();
    private PushButton uiAddButton = new PushButton(Message.messageAdmin().enumEditBtnAdd());
    private PushButton uiDelButton = new PushButton(Message.messageAdmin().enumEditBtnDel());
    private PushButton uiUpButton = new PushButton(Message.messageAdmin().enumEditBtnUp());
    private PushButton uiDownButton = new PushButton(Message.messageAdmin().enumEditBtnDown());

    public EnumEditor() {
        EmisUtils.init(this.uiAddButton, 40);
        EmisUtils.init(this.uiDelButton, 40);
        EmisUtils.init(this.uiUpButton, 40);
        EmisUtils.init(this.uiDownButton, 40);

        this.uiValues.setVisibleItemCount(15);
        this.uiValues.setWidth("100");
        this.uiValues.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            { EnumEditor.this.enableButtons(); }
        });
        this.uiAddButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                String value = Window.prompt(Message.messageAdmin().enumEditPromptEnterClassificationValue(), "");
                if ((value == null) || (value.equals("")))
                {
                    return;
                }
                for (int i = 0; i < EnumEditor.this.uiValues.getItemCount(); i++)
                {
                    if (value.equals(EnumEditor.this.uiValues.getItemText(i)))
                        return;
                }
                EnumEditor.this.uiValues.addItem(value);
                EnumEditor.this.enableButtons();
            }
        });
        this.uiDelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                int sel = EnumEditor.this.uiValues.getSelectedIndex();
                if (sel != -1)
                {
                    EnumEditor.this.uiValues.removeItem(sel);
                }
                if (sel < EnumEditor.this.uiValues.getItemCount())
                    EnumEditor.this.uiValues.setSelectedIndex(sel);
                else if (sel - 1 >= 0)
                {
                    EnumEditor.this.uiValues.setSelectedIndex(sel - 1);
                }
                EnumEditor.this.enableButtons();
            }
        });
        this.uiUpButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                int sel = EnumEditor.this.uiValues.getSelectedIndex();
                if (sel > 0)
                {
                    String value = EnumEditor.this.uiValues.getItemText(sel);
                    EnumEditor.this.uiValues.removeItem(sel);
                    EnumEditor.this.uiValues.insertItem(value, sel - 1);
                    EnumEditor.this.uiValues.setSelectedIndex(sel - 1);
                    EnumEditor.this.enableButtons();
                }
            }
        });
        this.uiDownButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                int sel = EnumEditor.this.uiValues.getSelectedIndex();
                if (sel + 1 < EnumEditor.this.uiValues.getItemCount())
                {
                    String value = EnumEditor.this.uiValues.getItemText(sel);
                    EnumEditor.this.uiValues.removeItem(sel);
                    EnumEditor.this.uiValues.insertItem(value, sel + 1);
                    EnumEditor.this.uiValues.setSelectedIndex(sel + 1);
                    EnumEditor.this.enableButtons();
                }
            }
        });
        getFlexCellFormatter().setColSpan(0, 0, 2);
        setWidget(1, 0, this.uiValues);

        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(1);
        vp.add(this.uiAddButton);
        vp.add(this.uiDelButton);
        vp.add(this.uiUpButton);
        vp.add(this.uiDownButton);
        setWidget(1, 1, vp);

    	getCellFormatter().setStyleName(0, 0, "sectionBar"); 
    	getFlexCellFormatter().setColSpan(0, 0, 2);
    	getCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
    	enableButtons();
    }

    private void enableButtons()
    {
        int pos = this.uiValues.getSelectedIndex();
        this.uiDelButton.setEnabled(pos != -1);
        this.uiUpButton.setEnabled(pos > 0);
        this.uiDownButton.setEnabled((pos != -1) && (pos + 1 < this.uiValues.getItemCount()));
    }

    public void set(EmisMetaEnum metaEnum)
    {
    	setHTML(0, 0, "Values for '" + metaEnum.getName() + "'");
        get();
        this.uiValues.clear();
        this.metaEnum = metaEnum;
        if (metaEnum == null)
        {
            enableButtons();
            return;
        }

        for (String value : metaEnum.getValues())
        {
            this.uiValues.addItem(value);
        }
        enableButtons();
    }

    public EmisMetaEnum get()
    {
        if (this.metaEnum == null)
        {
            return null;
        }
        String[] values = new String[this.uiValues.getItemCount()];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = this.uiValues.getItemText(i);
        }
        this.metaEnum.setValues(values);

        return this.metaEnum;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.EnumEditor JD-Core Version:
 * 0.6.0
 */