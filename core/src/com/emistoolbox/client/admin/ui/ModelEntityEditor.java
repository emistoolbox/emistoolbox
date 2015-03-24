package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.common.model.impl.MetaData;
import com.emistoolbox.common.model.impl.MetaEntity;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.util.NamedIndexList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class ModelEntityEditor extends FlexTable
{
    private NamedIndexList<EmisMetaEntity> entities = new NamedIndexList();
    private Tree uiTree = new Tree();
    private PushButton uiAddVariable = new PushButton(Message.messageAdmin().mEnteBtnAddVariable());
    private SimplePanel uiContainer = new SimplePanel();
    private EmisMetaDataEditor uiDataEditor;
    private EmisMetaEntityEditor uiEntityEditor;
    private EmisMeta emis;

    public ModelEntityEditor() {
        this.uiTree.setStylePrimaryName("emisTree");
        EmisUtils.init(this.uiAddVariable, 100);

        getColumnFormatter().setWidth(0, "250px"); 
        getColumnFormatter().setWidth(1, "300px"); 
        
        setHTML(0, 0, Message.messageAdmin().mEnteInfoLocations());

        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(2);
        PushButton btn = new PushButton(Message.messageAdmin().mEnteBtnAddLocation(), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                String name = EmisUtils.getUniqueId(ModelEntityEditor.this.entities, Message.messageAdmin().mEnteInfoNameOfLocation());
                if (name == null)
                {
                    return;
                }
                EmisMetaEntity entity = new MetaEntity();
                entity.setName(name);
                ModelEntityEditor.this.entities.add(entity);

                NamedIndexList datas = new NamedIndexList();
                EmisMetaData data = new MetaData();
                data.setEntity(entity);
                data.setType(EmisMetaData.EmisDataType.STRING);
                data.setDateType(ModelEntityEditor.this.emis.getDefaultDateType());
                data.setName("name");
                datas.add(data);
                entity.setData(datas);

                ModelEntityEditor.this.uiTree.setSelectedItem(ModelEntityEditor.this.addUi(entity));
                ModelEntityEditor.this.uiTree.ensureSelectedItemVisible();
            }
        });
        hp.add(EmisUtils.init(btn, 100));

        this.uiAddVariable.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                EmisMetaEntity entity = ModelEntityEditor.this.getSelectedEntity();
                if (entity == null)
                {
                    return;
                }
                String name = EmisUtils.getUniqueId(entity.getData(), Message.messageAdmin().mEnteInfoVariableName());
                if (name == null)
                {
                    return;
                }
                EmisMetaData data = new MetaData();
                data.setName(name);
                data.setEntity(entity);
                entity.getData().add(data);

                ModelEntityEditor.this.uiTree.setSelectedItem(ModelEntityEditor.this.addUi(data, ModelEntityEditor.this.getEntityItem(ModelEntityEditor.this.uiTree.getSelectedItem())));
                ModelEntityEditor.this.uiTree.ensureSelectedItemVisible();
            }
        });
        hp.add(this.uiAddVariable);

        btn = new PushButton(Message.messageAdmin().mEnteBtnDelete(), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                TreeItem item = ModelEntityEditor.this.uiTree.getSelectedItem();
                if (item == null)
                {
                    return;
                }
                TreeItem parent = null;
                if (item.getParentItem() == null)
                {
                    ModelEntityEditor.this.uiTree.removeItem(item);
                }
                else
                {
                    parent = item.getParentItem();
                    parent.removeItem(item);
                }

                Object userObj = item.getUserObject();
                if ((userObj != null) && ((item.getUserObject() instanceof EmisMetaData)))
                {
                    EmisMetaData data = (EmisMetaData) userObj;
                    if (parent != null)
                    {
                        EmisMetaEntity entity = (EmisMetaEntity) parent.getUserObject();
                        entity.getData().remove(data);
                    }
                }
                else if ((userObj != null) && ((item.getUserObject() instanceof EmisMetaEntity)))
                {
                    ModelEntityEditor.this.entities.remove(userObj);
                }
                ModelEntityEditor.this.updateSelection();
            }
        });
        hp.add(EmisUtils.init(btn, 100));

        getFlexCellFormatter().setColSpan(0, 0, 2);
        getFlexCellFormatter().setColSpan(1, 0, 2);
        setWidget(1, 0, hp);

        this.uiTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            public void onSelection(SelectionEvent<TreeItem> event)
            {
                ModelEntityEditor.this.updateSelection();
            }
        });
        getRowFormatter().setVerticalAlign(2, HasVerticalAlignment.ALIGN_TOP);
        setWidget(2, 0, this.uiTree);
        setWidget(2, 1, this.uiContainer);
    }

    private void updateSelection()
    {
        if (this.uiDataEditor == null)
        {
            this.uiDataEditor = new EmisMetaDataEditor(this.emis);
            uiDataEditor.setWidth("300px");
        }
        else
            this.uiDataEditor.get();
        this.uiDataEditor.set(null);

        if (this.uiEntityEditor == null)
        {
            this.uiEntityEditor = new EmisMetaEntityEditor();
            uiEntityEditor.setWidth("300px");
        }
        else
            this.uiEntityEditor.get();
        this.uiEntityEditor.set(null);

        TreeItem item = this.uiTree.getSelectedItem();
        if ((item != null) && ((item.getUserObject() instanceof EmisMetaData)))
        {
        	this.uiDataEditor.set((EmisMetaData) item.getUserObject());
        	uiContainer.setWidget(uiDataEditor);
        }
        else if ((item != null) && ((item.getUserObject() instanceof EmisMetaEntity)))
        {
            this.uiEntityEditor.set((EmisMetaEntity) item.getUserObject());
            this.uiContainer.setWidget(this.uiEntityEditor);
        }
        else
            this.uiContainer.setWidget(null);
    }

    private void setEmis(EmisMeta emis)
    {
        this.emis = emis;
        this.uiContainer.clear();
        this.uiDataEditor = null;
    }

    private EmisMetaEntity getSelectedEntity()
    {
        TreeItem item = this.uiTree.getSelectedItem();

        while (item != null)
        {
            if ((item.getUserObject() instanceof EmisMetaEntity))
                return (EmisMetaEntity) item.getUserObject();

            item = item.getParentItem();
        }

        return null;
    }

    private EmisMetaData getSelectedData()
    {
        TreeItem item = this.uiTree.getSelectedItem();
        if (item == null)
        {
            return null;
        }
        if ((item.getUserObject() instanceof EmisMetaData))
        {
            return (EmisMetaData) item.getUserObject();
        }
        return null;
    }

    private TreeItem getSelectedEntityTreeItem()
    {
        TreeItem item = this.uiTree.getSelectedItem();
        while (item != null)
        {
            if ((item.getUserObject() instanceof EmisMetaEntity))
            {
                return item;
            }
            item = item.getParentItem();
        }

        return null;
    }

    public void set(NamedIndexList<EmisMetaEntity> entities, EmisMeta emis)
    {
        this.uiTree.clear();

        setEmis(emis);
        this.entities = entities;
        for (EmisMetaEntity entity : entities)
            addUi(entity);
    }

    public NamedIndexList<EmisMetaEntity> get()
    {
        if (this.uiDataEditor != null)
        {
            this.uiDataEditor.get();
        }
        return this.entities;
    }

    public TreeItem addUi(EmisMetaEntity entity)
    {
        TreeItem item = new TreeItem(entity.getName());
        item.setUserObject(entity);

        if (entity.getData() != null)
        {
            for (EmisMetaData data : entity.getData())
            {
                addUi(data, item);
            }
        }
        this.uiTree.addItem(item);
        return item;
    }

    public TreeItem addUi(EmisMetaData data, TreeItem parent)
    {
        TreeItem item = new TreeItem(data.getName());
        item.setUserObject(data);
        parent.addItem(item);
        return item;
    }

    public void refresh()
    {
        if (this.uiDataEditor == null)
        {
            return;
        }
        EmisMetaData tmp = this.uiDataEditor.get();
        this.uiDataEditor.refresh();
        this.uiDataEditor.set(tmp);
    }

    private TreeItem getEntityItem(TreeItem item)
    {
        if (item == null)
        {
            return null;
        }
        while ((item.getParentItem() != null) && (!(item.getUserObject() instanceof EmisMetaEntity)))
        {
            item = item.getParentItem();
        }
        return item;
    }
}
