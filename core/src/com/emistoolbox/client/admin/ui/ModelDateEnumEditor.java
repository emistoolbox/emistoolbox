package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.common.model.impl.MetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.util.Named;
import com.emistoolbox.common.util.NamedIndexList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import java.util.List;

public class ModelDateEnumEditor extends FlexTable
{
    private Tree uiTree = new Tree();
    private EnumEditor uiEnum = new EnumEditor();

    public ModelDateEnumEditor() {
        this.uiTree.setStylePrimaryName("emisTree");

        setHTML(0, 0, Message.messageAdmin().mdeeInfoDateAnalysis());
        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(1);
        PushButton btn = new PushButton(Message.messageAdmin().mdeeBtnAddDate(), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                String name = EmisUtils.getUniqueId((List) null, Message.messageAdmin().mdeePromptEnterDateEnumName());

                if ((name == null) || (name.equals("")))
                {
                    return;
                }
                EmisMetaDateEnum newEnum = new MetaDateEnum();
                newEnum.setName(name);
                TreeItem item = ModelDateEnumEditor.this.uiTree.getSelectedItem();
                if (item != null)
                    newEnum.setParent((EmisMetaDateEnum) item.getUserObject());

                TreeItem newItem = ModelDateEnumEditor.this.addUi(newEnum);
                ModelDateEnumEditor.this.uiTree.setSelectedItem(newItem);
                ModelDateEnumEditor.this.uiTree.ensureSelectedItemVisible();

                ModelDateEnumEditor.this.uiEnum.set(newEnum);
            }
        });
        hp.add(EmisUtils.init(btn, 80));

        btn = new PushButton(Message.messageAdmin().mdeeBtnDelDate(), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                TreeItem parent = ModelDateEnumEditor.this.uiTree.getSelectedItem();
                if (parent != null)
                {
                    parent = parent.getParentItem();
                }
                ModelDateEnumEditor.this.deleteItem(ModelDateEnumEditor.this.uiTree.getSelectedItem());
                if (parent != null)
                    ModelDateEnumEditor.this.uiTree.setSelectedItem(parent);
            }
        });
        hp.add(EmisUtils.init(btn, 80));

        this.uiTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            public void onSelection(SelectionEvent<TreeItem> event)
            {
                ModelDateEnumEditor.this.commit();
                if (event.getSelectedItem() != null)
                {
                    ModelDateEnumEditor.this.uiEnum.set((EmisMetaDateEnum) ((TreeItem) event.getSelectedItem()).getUserObject());
                    ModelDateEnumEditor.this.setWidget(2, 1, ModelDateEnumEditor.this.uiEnum);
                }
                else
                {
                    ModelDateEnumEditor.this.clearCell(2, 1);
                }
            }
        });
        getFlexCellFormatter().setColSpan(1, 0, 2);
        setWidget(1, 0, hp);

        getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
        setWidget(2, 0, this.uiTree);
        setWidget(3, 1, null);
    }

    private void commit()
    {
        this.uiEnum.get();
    }

    private void deleteItem(TreeItem item)
    {
        if (item == null)
        {
            return;
        }
        while (item.getChildCount() > 0)
        {
            deleteItem(item.getChild(0));
        }
        TreeItem parent = item.getParentItem();
        if (parent == null)
            this.uiTree.removeItem(item);
        else
            parent.removeItem(item);
    }

    public void set(NamedIndexList<EmisMetaDateEnum> enums)
    {
        this.uiTree.clear();
        for (EmisMetaDateEnum item : enums)
            addUi(item);
    }

    public NamedIndexList<EmisMetaDateEnum> get()
    {
        commit();
        NamedIndexList result = new NamedIndexList();
        for (int i = 0; i < this.uiTree.getItemCount(); i++)
        {
            addItems(result, this.uiTree.getItem(i));
        }
        return result;
    }

    private void addItems(NamedIndexList<EmisMetaDateEnum> result, TreeItem item)
    {
        result.add((EmisMetaDateEnum) item.getUserObject());
        for (int i = 0; i < item.getChildCount(); i++)
            addItems(result, item.getChild(i));
    }

    private TreeItem addUi(EmisMetaDateEnum dateEnum)
    {
        TreeItem item = findItem(dateEnum.getName());
        if (item != null)
        {
            return item;
        }
        EmisMetaEnum[] enums = dateEnum.getEnums();

        TreeItem newItem = new TreeItem();
        newItem.setText(dateEnum.getName());
        newItem.setUserObject(dateEnum);

        if (enums.length == 1)
        {
            this.uiTree.addItem(newItem);
        }
        else
        {
            EmisMetaDateEnum parentDate = (EmisMetaDateEnum) enums[(enums.length - 2)];
            TreeItem parentItem = addUi(parentDate);
            parentItem.addItem(newItem);
        }

        return newItem;
    }

    private TreeItem findItem(String name)
    {
        for (int i = 0; i < this.uiTree.getItemCount(); i++)
        {
            if (((Named) this.uiTree.getItem(i).getUserObject()).getName().equals(name))
            {
                return this.uiTree.getItem(i);
            }
        }
        return null;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.ModelDateEnumEditor JD-Core
 * Version: 0.6.0
 */