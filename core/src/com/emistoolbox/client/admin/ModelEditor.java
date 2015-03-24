package com.emistoolbox.client.admin;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.ui.ModelDateEnumEditor;
import com.emistoolbox.client.admin.ui.ModelEntityEditor;
import com.emistoolbox.client.admin.ui.ModelEnumEditor;
import com.emistoolbox.client.admin.ui.ModelGisEditor;
import com.emistoolbox.client.admin.ui.ModelHierarchyEditor;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ModelEditor extends VerticalPanel implements EmisEditor<EmisMeta>
{
    private EmisToolbox app;
    private TabPanel tabs = new TabPanel();
    private EmisMeta metaModel;
    int currentTab = -1;
    int entityTabIndex = -1;
    int enumTabIndex = -1;
    int dateEnumTabIndex = -1;
    int hierarchyTabIndex = -1;
    int gisTabIndex = -1;
    private ModelEntityEditor entityEditor;
    private ModelEnumEditor enumEditor;
    private ModelDateEnumEditor dateEnumEditor;
    private ModelHierarchyEditor hierarchyEditor;
    private ModelGisEditor gisEditor;

    public ModelEditor(EmisToolbox app) {
        setWidth("100%");
        this.app = app;

        clear();

        this.enumEditor = new ModelEnumEditor();
        this.dateEnumEditor = new ModelDateEnumEditor();
        this.entityEditor = new ModelEntityEditor();
        this.hierarchyEditor = new ModelHierarchyEditor();
        this.gisEditor = new ModelGisEditor();

        this.tabs.setStylePrimaryName("emisTabs");
        this.tabs.addStyleName("wide");
        this.tabs.clear();
        this.enumTabIndex = this.tabs.getWidgetCount();
        this.tabs.add(this.enumEditor, Message.messageAdmin().meTabClassifications());

        this.dateEnumTabIndex = this.tabs.getWidgetCount();
        this.tabs.add(this.dateEnumEditor, Message.messageAdmin().meTabDates());

        this.entityTabIndex = this.tabs.getWidgetCount();
        this.tabs.add(this.entityEditor, Message.messageAdmin().meTabLocations());

        this.hierarchyTabIndex = this.tabs.getWidgetCount();
        this.tabs.add(this.hierarchyEditor, Message.messageAdmin().meTabHierarchies());

        this.gisTabIndex = this.tabs.getWidgetCount();
        this.tabs.add(this.gisEditor, Message.messageAdmin().meTabGis());

        this.currentTab = 0;
        this.tabs.selectTab(0);

        setHorizontalAlignment(ALIGN_LEFT);
        add(this.tabs);

        this.tabs.addSelectionHandler(new SelectionHandler<Integer>() {
            public void onSelection(SelectionEvent<Integer> event)
            {
                ModelEditor.this.commit();
                ModelEditor.this.currentTab = ((Integer) event.getSelectedItem()).intValue();
                if (ModelEditor.this.currentTab == ModelEditor.this.entityTabIndex)
                    ModelEditor.this.entityEditor.refresh();
            }
        });
    }

    public void commit()
    {
        if (this.currentTab == this.entityTabIndex)
            this.metaModel.setEntities(this.entityEditor.get());
        else if (this.currentTab == this.enumTabIndex)
            this.metaModel.setEnums(this.enumEditor.get());
        else if (this.currentTab == this.dateEnumTabIndex)
            this.metaModel.setDateEnums(this.dateEnumEditor.get());
        else if (this.currentTab == this.hierarchyTabIndex)
            this.metaModel.setHierarchies(this.hierarchyEditor.get());
        else if (this.currentTab == this.gisTabIndex)
            this.metaModel.setGisContext(this.gisEditor.get());
    }

    public void set(EmisMeta newEmis)
    {
        this.metaModel = newEmis;

        this.entityEditor.set(newEmis.getEntities(), newEmis);
        this.enumEditor.set(newEmis.getEnums());
        this.dateEnumEditor.set(newEmis.getDateEnums());
        this.hierarchyEditor.set(newEmis.getHierarchies(), newEmis);
        this.gisEditor.set(newEmis.getGisContext());
    }

    public EmisMeta get()
    {
        return this.metaModel;
    }
}
