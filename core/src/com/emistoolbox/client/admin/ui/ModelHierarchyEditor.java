package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.client.util.ui.NamedSelectPopup;
import com.emistoolbox.common.model.impl.MetaHierarchy;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.NamedIndexList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.ArrayList;
import java.util.List;

public class ModelHierarchyEditor extends FlexTable
{
    private NamedIndexList<EmisMetaHierarchy> hierarchies;
    private Tree uiTree = new Tree();
    private PushButton uiAddHierarchyButton = new PushButton(Message.messageAdmin().mheBtnAddHierarchy());
    private PushButton uiAddEntityButton = new PushButton(Message.messageAdmin().mheBtnAddLocation());
    private PushButton uiDelButton = new PushButton(Message.messageAdmin().mheBtnDelete());
    private PushButton uiUpButton = new PushButton(Message.messageAdmin().mheBtnUp());
    private PushButton uiDownButton = new PushButton(Message.messageAdmin().mheBtnDown());
    private EmisMeta emis;

    public ModelHierarchyEditor() {
        this.uiTree.setStylePrimaryName("emisTree");
        EmisUtils.init(this.uiAddHierarchyButton, 100);
        EmisUtils.init(this.uiAddEntityButton, 100);
        EmisUtils.init(this.uiDelButton, 100);
        EmisUtils.init(this.uiUpButton, 100);
        EmisUtils.init(this.uiDownButton, 100);

        setHTML(0, 0, Message.messageAdmin().mheInfoHierarchiesRelationships());

        this.uiAddHierarchyButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                String name = EmisUtils.getUniqueId(ModelHierarchyEditor.this.uiTree, Message.messageAdmin().mhePromptInputHierarchyName());
                if ((name == null) || (name.equals("")))
                {
                    return;
                }
                TreeItem item = new TreeItem(); 
                item.setText(name);
                EmisMetaHierarchy hierarchy = new MetaHierarchy();
                hierarchy.setName(name);
                item.setUserObject(hierarchy);

                ModelHierarchyEditor.this.uiTree.addItem(item);
                ModelHierarchyEditor.this.uiTree.setSelectedItem(item);
                ModelHierarchyEditor.this.uiTree.ensureSelectedItemVisible();
                ModelHierarchyEditor.this.enableButtons();
            }
        });
        this.uiAddEntityButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                List entities = new ArrayList();
                entities.addAll(ModelHierarchyEditor.this.emis.getEntities());

                final TreeItem hierarchyItem = ModelHierarchyEditor.this.getSelectedHierarchyItem();
                if (hierarchyItem == null)
                    return;
                entities.removeAll(ModelHierarchyEditor.this.getEntities(hierarchyItem));
                if (entities.size() == 0)
                {
                    return;
                }

                String htmlString = "<b>" + Message.messageAdmin().mhePromptSelectEntity() + " <p><strong>" + Message.messageAdmin().mhePromptChooseTopEntityFirst() + "</strong></b><hr>";

                NamedSelectPopup popup = new NamedSelectPopup(htmlString, entities, null);

                popup.addValueChangeHandler(new ValueChangeHandler<EmisMetaEntity>() {
                    public void onValueChange(ValueChangeEvent<EmisMetaEntity> event)
                    {
                        TreeItem item = new TreeItem();
                        item.setText(((EmisMetaEntity) event.getValue()).getName());
                        item.setUserObject(event.getValue());
                        hierarchyItem.addItem(item);
                        ModelHierarchyEditor.this.uiTree.setSelectedItem(item);
                        ModelHierarchyEditor.this.uiTree.ensureSelectedItemVisible();
                    }
                });
                popup.showRelativeTo(ModelHierarchyEditor.this.uiAddEntityButton);
            }
        });
        this.uiDelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                TreeItem item = ModelHierarchyEditor.this.uiTree.getSelectedItem();
                if (item == null)
                {
                    return;
                }
                if (item.getParentItem() == null)
                    ModelHierarchyEditor.this.uiTree.removeItem(item);
                else
                    item.getParentItem().removeItem(item);
            }
        });
        this.uiUpButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                TreeItem item = ModelHierarchyEditor.this.uiTree.getSelectedItem();
                if ((item == null) || (item.getParentItem() == null))
                {
                    return;
                }
                GwtUtils.move(item, -1);
                ModelHierarchyEditor.this.uiTree.setSelectedItem(item);
            }
        });
        this.uiDownButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                TreeItem item = ModelHierarchyEditor.this.uiTree.getSelectedItem();
                if ((item == null) || (item.getParentItem() == null))
                {
                    return;
                }
                GwtUtils.move(item, 1);
                ModelHierarchyEditor.this.uiTree.setSelectedItem(item);
            }
        });
        this.uiTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            public void onSelection(SelectionEvent<TreeItem> event)
            {
                ModelHierarchyEditor.this.enableButtons();
            }
        });
        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(1);
        vp.add(this.uiAddHierarchyButton);
        vp.add(this.uiAddEntityButton);
        vp.add(this.uiDelButton);
        vp.add(this.uiUpButton);
        vp.add(this.uiDownButton);

        getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
        setWidget(2, 0, this.uiTree);
        setWidget(2, 1, vp);

        enableButtons();
    }

    private void setEmis(EmisMeta emis)
    {
        this.emis = emis;
    }

    private TreeItem getSelectedHierarchyItem()
    {
        TreeItem parent = this.uiTree.getSelectedItem();
        if (parent == null)
        {
            return null;
        }
        while (parent.getParentItem() != null)
        {
            parent = parent.getParentItem();
        }
        return parent;
    }

    private NamedIndexList<EmisMetaEntity> getEntities(TreeItem parentItem)
    {
        NamedIndexList result = new NamedIndexList();
        if (parentItem == null)
        {
            return result;
        }
        for (int i = 0; i < parentItem.getChildCount(); i++)
        {
            TreeItem childItem = parentItem.getChild(i);
            if ((childItem.getUserObject() instanceof EmisMetaEntity))
            {
                result.add((EmisMetaEntity) childItem.getUserObject());
            }
        }
        return result;
    }

    private void enableButtons()
    {
        TreeItem item = this.uiTree.getSelectedItem();
        this.uiAddHierarchyButton.setEnabled(true);
        this.uiAddEntityButton.setEnabled(item != null);
        this.uiDelButton.setEnabled(item != null);

        if ((item != null) && (item.getParentItem() != null))
        {
            int index = GwtUtils.getChildIndex(item);
            this.uiUpButton.setEnabled(index > 0);
            this.uiDownButton.setEnabled(index + 1 < GwtUtils.getSiblingCount(item));
        }
        else
        {
            this.uiUpButton.setEnabled(false);
            this.uiDownButton.setEnabled(false);
        }
    }

    public void set(NamedIndexList<EmisMetaHierarchy> hierarchies, EmisMeta emis)
    {
        this.emis = emis;
        this.hierarchies = hierarchies;

        this.uiTree.clear();
        for (EmisMetaHierarchy hierarchy : hierarchies)
        {
            TreeItem newItem = new TreeItem(); 
            newItem.setText(hierarchy.getName());
            newItem.setUserObject(hierarchy);
            this.uiTree.addItem(newItem);

            for (EmisMetaEntity entity : hierarchy.getEntityOrder())
            {
                TreeItem newEntity = new TreeItem(); 
                newItem.setText(entity.getName());
                newEntity.setUserObject(entity);
                newItem.addItem(newEntity);
            }
        }
        enableButtons();
    }

    public NamedIndexList<EmisMetaHierarchy> get()
    {
        this.hierarchies.clear();
        for (int i = 0; i < this.uiTree.getItemCount(); i++)
        {
            TreeItem item = this.uiTree.getItem(i);
            EmisMetaHierarchy hierarchy = (EmisMetaHierarchy) item.getUserObject();
            hierarchy.setEntityOrder(getEntities(item));
            this.hierarchies.add(hierarchy);
        }

        return this.hierarchies;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.ModelHierarchyEditor JD-Core
 * Version: 0.6.0
 */