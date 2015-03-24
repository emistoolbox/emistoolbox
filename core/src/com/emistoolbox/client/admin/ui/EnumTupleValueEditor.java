package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

public class EnumTupleValueEditor extends HorizontalPanel implements EmisEditor<EmisEnumTupleValue>, HasValueChangeHandlers<EmisEnumTupleValue>
{
    private EmisEnumTupleValue value;
    private ListBox[] uiValues;
    private boolean withOk;

    public EnumTupleValueEditor(boolean withOk) {
        this.withOk = withOk;
    }

    public void commit()
    {
        byte[] values = new byte[this.uiValues.length];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = (byte) this.uiValues[i].getSelectedIndex();
        }
        this.value.setIndex(values);
    }

    public EmisEnumTupleValue get()
    {
        commit();
        return this.value;
    }

    public void set(EmisEnumTupleValue value)
    {
        clear();
        this.value = value;

        EmisMetaEnum[] enums = value.getEnumTuple().getEnums();
        byte[] indexes = value.getIndex();

        this.uiValues = new ListBox[enums.length];
        for (int i = 0; i < enums.length; i++)
        {
            this.uiValues[i] = new ListBox();
            add(initListBox(this.uiValues[i], enums[i], indexes[i]));
        }

        if (!this.withOk)
            return;

        HTML btnOk = new HTML(Message.messageAdmin().btnOk());
        btnOk.setStyleName("smallButton");
        btnOk.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { ValueChangeEvent.fire(EnumTupleValueEditor.this, EnumTupleValueEditor.this.get()); }
        });
        add(btnOk);
    }

    private ListBox initListBox(ListBox lb, EmisMetaEnum enumType, byte selectedIndex)
    {
        lb.clear();
        for (String value : enumType.getValues())
            lb.addItem(value);

        if (selectedIndex != -1)
            lb.setSelectedIndex(selectedIndex);

        return lb;
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<EmisEnumTupleValue> handler)
    {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
