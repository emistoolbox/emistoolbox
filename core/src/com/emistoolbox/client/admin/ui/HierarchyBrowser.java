package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.StatusAsyncCallback;
import com.emistoolbox.client.ui.results.IHierarchyBrowser;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HierarchyBrowser extends HierarchyBrowserBase implements IHierarchyBrowser
{
    private static final int SELECT_ROW = 2;
    private FlexTable ui = new FlexTable();
    private ListBox uiDate = null;

    public HierarchyBrowser(EmisToolbox emis, List<EmisEntity> rootEntities) 
    { super(emis, rootEntities); }

    public void setEmisMeta(EmisMeta meta, EmisMetaHierarchy newHierarchy)
    {
        super.setEmisMeta(meta, newHierarchy);

        this.ui.clear(true);
        if ((newHierarchy == null) && (meta.getHierarchies().size() == 0))
        {
            this.ui.setHTML(0, 0, Message.messageAdmin().hbNoHeirarchiesDefined());
            return;
        }
    }

    public void setHierarchy(EmisMetaHierarchy hierarchy)
    {
        super.setHierarchy(hierarchy);

        this.ui.setHTML(1, 0, "<b>" + Message.messageAdmin().hbLabelHierarchy() + ":</b>");
        this.ui.setText(1, 1, hierarchy.getName());
    }

    public void updateUi()
    {
        updateUi(0);
    }

    public void updateUi(int entityIndex)
    {
        final int entityLevel = entityIndex;

        if ((getEntityType() == null) && (!hasAnySelection()))
        {
            return;
        }
        for (int i = entityIndex; i < getPathIds().length; i++)
        {
            setPathId(i, -1);
        }
        EmisMetaEntity parentEntity = null;
        int parentId = -1;
        while ((entityIndex > 0) && (parentId == -1))
        {
            parentId = getPathId(entityIndex - 1);
            parentEntity = (EmisMetaEntity) getHierarchy().getEntityOrder().get(entityIndex - 1);
            if (parentId == -1)
            {
                entityIndex--;
            }
        }
        if (entityIndex == 0)
        {
            parentEntity = null;
        }
        for (int i = 0; i < entityIndex; i++)
        {
            this.ui.setHTML(2 + i, 0, "<b>" + ((EmisMetaEntity) getHierarchy().getEntityOrder().get(i)).getName() + ":</b>");
            HTML html = new HTML(getPathName(i));
            html.setStyleName("pointer");
            final int tmpEntityIndex = i;
            html.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event)
                {
                    HierarchyBrowser.this.updateUi(tmpEntityIndex);
                }
            });
            this.ui.setWidget(2 + i, 1, html);
        }

        final int updateIndex = entityIndex;

        for (int i = 2 + updateIndex + 1; i < this.ui.getRowCount(); i++)
        {
            this.ui.removeRow(i);
        }
        if (entityIndex >= getSize())
        {
            return;
        }
        EmisMetaEntity entity = (EmisMetaEntity) getHierarchy().getEntityOrder().get(entityIndex);
        this.ui.getCellFormatter().setVerticalAlignment(2 + updateIndex, 0, HasVerticalAlignment.ALIGN_TOP);
        this.ui.setHTML(2 + updateIndex, 0, "<b>" + entity.getName() + ":</b>");
        this.ui.setHTML(2 + updateIndex, 1, Message.messageAdmin().hbMessageLoading());

        getHierarchyEntities(parentEntity, parentId, new StatusAsyncCallback<Map<Integer, String>>("") {
            public void onSuccess(Map<Integer, String> result)
            {
                VerticalPanel vp = new VerticalPanel();
                vp.setSpacing(1);

                if (result.size() == 0)
                {
                    HierarchyBrowser.this.ui.setWidget(2 + updateIndex, 1, new Label(Message.messageAdmin().hbLabelNoEntitiesFound()));
                    return;
                }

                List<Map.Entry<Integer, String>> entries = new ArrayList<Map.Entry<Integer, String>>();
                entries.addAll(result.entrySet());
                Collections.sort(entries, new Comparator<Map.Entry<Integer, String>>() {
                    public int compare(Map.Entry<Integer, String> o1, Map.Entry<Integer, String> o2)
                    {
                        return ((String) o1.getValue()).compareTo((String) o2.getValue());
                    }
                });
                if ((HierarchyBrowser.this.hasAnySelection()) && (entityLevel == 0))
                {
                    HTML uiSelect = new HTML(Message.messageAdmin().hbSelectorSelectAll());
                    uiSelect.setStylePrimaryName("textButton");
                    uiSelect.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event)
                        {
                            for (int i = 0; i < HierarchyBrowser.this.getPathIds().length; i++)
                            {
                                HierarchyBrowser.this.setPath(i, -1, null);
                            }
                            ValueChangeEvent.fire(HierarchyBrowser.this, HierarchyBrowser.this.get());
                        }
                    });
                    vp.add(uiSelect);
                }

                for (final Map.Entry<Integer, String> entry : entries)
                {
                    Label l = new Label((String) entry.getValue());
                    l.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event)
                        {
                            HierarchyBrowser.this.setPath(updateIndex, entry.getKey().intValue(), entry.getValue());
                            if ((HierarchyBrowser.this.getEntityType() != null) && (HierarchyBrowser.this.getEntityTypeIndex() == updateIndex))
                                ValueChangeEvent.fire(HierarchyBrowser.this, HierarchyBrowser.this.get());
                            else
                                updateUi(updateIndex + 1);
                        }
                    });
                    l.setStyleName("pointer");
                    if (HierarchyBrowser.this.hasAnySelection())
                    {
                        HorizontalPanel hp = new HorizontalPanel();
                        hp.add(l);
                        HTML uiSelect = new HTML(Message.messageAdmin().hbSelectorSelect());
                        uiSelect.setStylePrimaryName("textButton");
                        uiSelect.addClickHandler(new ClickHandler() {
                            public void onClick(ClickEvent event)
                            {
                                HierarchyBrowser.this.setPath(updateIndex, entry.getKey().intValue(), entry.getValue());
                                ValueChangeEvent.fire(HierarchyBrowser.this, HierarchyBrowser.this.get());
                            }
                        });
                        hp.add(uiSelect);
                        vp.add(hp);
                    }
                    else
                    {
                        vp.add(l);
                    }
                }
                HierarchyBrowser.this.ui.setWidget(2 + updateIndex, 1, vp);
            }
        });
    }

    public int getDateIndex()
    {
        return this.uiDate == null ? super.getDateIndex() : this.uiDate.getSelectedIndex();
    }

    public EmisEntity get()
    {
        int index = 0;
        while ((index < getPathIds().length) && (getPathId(index) != -1))
        {
            index++;
        }
        if (index == 0)
        {
            if (!hasAnySelection())
            {
                return null;
            }
            EmisEntity result = new Entity();
            result.setName("All");
            return result;
        }

        EmisEntity result = new Entity();
        result.setEntityType((EmisMetaEntity) getHierarchy().getEntityOrder().get(index - 1));
        result.setId(getPathId(index - 1));
        result.setName(getPathName(index - 1));

        return result;
    }

    public void setPath(int[] ids, String[] names)
    {
        super.setPath(ids, names);
        if (ids != null)
            updateUi(ids.length - 1);
    }

    public Widget getWidget()
    {
        return this.ui;
    }
}
