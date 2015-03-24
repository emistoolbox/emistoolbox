package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.common.model.impl.MetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.util.NamedIndexList;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;

public class ModelEnumEditor extends FlexTable
{
    private NamedIndexList<EmisMetaEnum> enums;
    private ListBox uiEnums = new ListBox();
    private EnumEditor uiEnum = new EnumEditor();

    public ModelEnumEditor() {
        setHTML(0, 0, "");
        this.uiEnums.setVisibleItemCount(20);
        this.uiEnums.setWidth("200");

        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(2);
        PushButton btn = new PushButton(Message.messageAdmin().mEnumeBtnNewClassification(), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                String name = EmisUtils.getUniqueId(ModelEnumEditor.this.enums, Message.messageAdmin().mEnumePromptInputClassificationName());
                if ((name == null) || (name.equals("")))
                {
                    return;
                }
                EmisMetaEnum newEnum = new MetaEnum();
                newEnum.setName(name);

                ModelEnumEditor.this.enums.add(newEnum);
                ModelEnumEditor.this.uiEnums.addItem(name);
                ModelEnumEditor.this.uiEnums.setSelectedIndex(ModelEnumEditor.this.uiEnums.getItemCount() - 1);
                ModelEnumEditor.this.showEnum();
            }
        });
        hp.add(EmisUtils.init(btn, 100));

        btn = new PushButton(Message.messageAdmin().mEnumeBtnDeleteClassification(), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                int index = ModelEnumEditor.this.uiEnums.getSelectedIndex();
                if (index == -1)
                    return;
                ModelEnumEditor.this.uiEnums.removeItem(index);
                ModelEnumEditor.this.enums.remove(index);

                if (index >= ModelEnumEditor.this.uiEnums.getItemCount())
                    index = ModelEnumEditor.this.uiEnums.getItemCount() - 1;
                ModelEnumEditor.this.uiEnums.setSelectedIndex(index);

                ModelEnumEditor.this.showEnum();
            }
        });
        hp.add(EmisUtils.init(btn, 100));

        this.uiEnums.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                ModelEnumEditor.this.showEnum();
            }
        });
        getFlexCellFormatter().setColSpan(0, 0, 2);
        getFlexCellFormatter().setColSpan(1, 0, 2);
        setWidget(1, 0, hp);

        getRowFormatter().setVerticalAlign(2, HasVerticalAlignment.ALIGN_TOP);
        setWidget(2, 0, this.uiEnums);
        setWidget(2, 1, null);
    }

    private void showEnum()
    {
        commit();

        int index = this.uiEnums.getSelectedIndex();
        if (index == -1)
            clearCell(2, 1);
        else
        {
            this.uiEnum.set(enums.get(index));
            setWidget(2, 1, this.uiEnum);
        }
    }

    public void set(NamedIndexList<EmisMetaEnum> enums)
    {
        this.uiEnums.clear();
        this.enums = enums;
        for (EmisMetaEnum item : enums)
            this.uiEnums.addItem(item.getName());
    }

    public NamedIndexList<EmisMetaEnum> get()
    {
        commit();
        return this.enums;
    }

    private void commit()
    {
        if (getWidget(2, 1) != null)
            this.uiEnum.get();
    }
}
