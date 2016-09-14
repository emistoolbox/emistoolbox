package com.emistoolbox.client.ui.results;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.EnumTupleValueEditor;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.client.admin.ui.TreeHierarchyBrowser;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.MetaResultDimensionUtil;
import com.emistoolbox.common.results.impl.MetaResultDimensionDate;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntity;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityAncestors;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityChildren;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityGrandChildren;
import com.emistoolbox.common.results.impl.MetaResultDimensionEnum;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

public class MetaDimensionEditor extends VerticalPanel implements EmisEditor<MetaResultDimension>, HasValueChangeHandlers<MetaResultDimension>
{
    private ListBoxWithUserObjects<MetaResultDimension> uiTypes = new ListBoxWithUserObjects<MetaResultDimension>();
    private SimplePanel uiTypeContainer = new SimplePanel();
    private SimplePanel uiContainer = new SimplePanel();
    private Label uiTypeLabel = new Label("");
    private EmisToolbox toolbox;
    private EmisMeta emisMeta;
    private EmisMetaHierarchy hierarchy;
    private List<EmisEntity> rootEntities; 
    private EmisReportConfig reportConfig; 

    public MetaDimensionEditor(EmisToolbox toolbox, EmisMeta emisMeta, EmisReportConfig reportConfig, EmisMetaHierarchy hierarchy, EmisIndicator indicator, MetaResultDimension ignoreDimension, List<EmisEntity> rootEntities) 
    {
        this.toolbox = toolbox;
        this.emisMeta = emisMeta;
        this.reportConfig = reportConfig; 
        this.hierarchy = hierarchy;
        this.rootEntities = rootEntities; 
        
        MessageAdmin msgAdmin = Message.messageAdmin(); 
        this.uiTypes.add("", null);
        if (ignoreDimension != null)
            this.uiTypes.add(Message.messageAdmin().metadeInfoNoSplitDimension(), null);

        if ((ignoreDimension == null) || (!(ignoreDimension instanceof MetaResultDimensionEntity)))
        {
            EmisMetaEntity lowestEntity = indicator.getSeniorEntity(hierarchy); 
            addDimensions(msgAdmin.metadeInfoHorizontalAnalysis(), MetaResultDimensionUtil.getHorizontalDimensions(msgAdmin, lowestEntity, hierarchy)); 
            addDimensions(msgAdmin.metadeInfoVerticalAnalysis(), MetaResultDimensionUtil.getVerticalDimensions(msgAdmin, lowestEntity, hierarchy)); 
        }

        // Add date dimensions. 
        EmisMetaDateEnum ignoreDateType = null; 
        if (ignoreDimension instanceof MetaResultDimensionDate)
        	ignoreDateType = ((MetaResultDimensionDate) ignoreDimension).getDateEnumType(); 
        addDimensions(msgAdmin.metadeInfoDateAnalysis(), MetaResultDimensionUtil.getDateDimensions(msgAdmin, indicator, ignoreDateType, reportConfig)); 

        if ((ignoreDimension instanceof MetaResultDimensionEnum))
            addDimensions(msgAdmin.metadeInfoEnumAnalysis(), MetaResultDimensionUtil.getEnumDimensions(msgAdmin, indicator, ((MetaResultDimensionEnum) ignoreDimension).getEnumType(), reportConfig)); 
        else
            addDimensions(msgAdmin.metadeInfoEnumAnalysis(), MetaResultDimensionUtil.getEnumDimensions(msgAdmin, indicator, null, reportConfig)); 

        addDimensions(msgAdmin.metadeInfoByEntityVariables(), MetaResultDimensionUtil.getEntityFilterDimensions(msgAdmin, indicator.getSeniorEntity(hierarchy), indicator, ignoreDimension));

        this.uiTypes.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                if ((MetaDimensionEditor.this.uiTypes.getValue() == null) && (MetaDimensionEditor.this.uiTypes.getItemText() != null) && (MetaDimensionEditor.this.uiTypes.getItemText().startsWith("--")))
                    return;

                MetaDimensionEditor.this.updateUi();
            }
        });
        this.uiTypeLabel.addStyleName("pointer");
        this.uiTypeLabel.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                MetaDimensionEditor.this.uiTypeContainer.setWidget(MetaDimensionEditor.this.uiTypes);
                MetaDimensionEditor.this.uiContainer.clear();
            }
        });
        this.uiTypeContainer.setWidget(this.uiTypes);
        add(this.uiTypeContainer);
        add(this.uiContainer);
    }

    private void setTypeLabel(MetaResultDimension dim)
    {
        this.uiTypeLabel.setText(dim.getName());
        this.uiTypeContainer.setWidget(this.uiTypeLabel);
    }

    private void updateUi()
    {
        final MetaResultDimension metaDimension = (MetaResultDimension) this.uiTypes.getValue();
        if ((metaDimension instanceof MetaResultDimensionDate))
        {
            EmisEnumTupleValue value = ((MetaResultDimensionDate) metaDimension).getDateEnum();
            if (value == null)
            {
                this.uiContainer.clear();
                ValueChangeEvent.fire(this, metaDimension);
                return;
            }

            EnumTupleValueEditor editor = new EnumTupleValueEditor(true);
            editor.set(value);
            editor.addValueChangeHandler(new ValueChangeHandler<EmisEnumTupleValue>() {
                public void onValueChange(ValueChangeEvent<EmisEnumTupleValue> event)
                {
                    ((MetaResultDimensionDate) metaDimension).setDateEnum((EmisEnumTupleValue) event.getValue());
                    ValueChangeEvent.fire(MetaDimensionEditor.this, metaDimension);
                }
            });
            setTypeLabel(metaDimension);
            this.uiContainer.setWidget(editor);
        }
        else if (metaDimension instanceof MetaResultDimensionEntityChildren || metaDimension instanceof MetaResultDimensionEntityGrandChildren)
        {
            final MetaResultDimensionEntity childrenDimension = (MetaResultDimensionEntity) metaDimension;
            if (childrenDimension.getEntityType() == null)
            {
                childrenDimension.setPath(new int[0], new String[0], 0);
                ValueChangeEvent.fire(this, metaDimension);

                return;
            }

            final IHierarchyBrowser editor = new TreeHierarchyBrowser(this.toolbox, rootEntities);
            editor.setEntityType(childrenDimension.getEntityType());
            editor.setEmisMeta(this.emisMeta, this.hierarchy);
            editor.setPath(childrenDimension.getEntityPath(), childrenDimension.getEntityPathNames());
            editor.addValueChangeHandler(new ValueChangeHandler<EmisEntity>() {
                public void onValueChange(ValueChangeEvent<EmisEntity> event)
                {
                    childrenDimension.setPath(editor.getPathIds(), editor.getPathNames(), editor.getDateIndex());
                    ValueChangeEvent.fire(MetaDimensionEditor.this, metaDimension);
                }
            });
            editor.getWidget().addStyleName("emisEdit");

            setTypeLabel(metaDimension);
            this.uiContainer.setWidget(editor.getWidget());
        }
        else if ((metaDimension instanceof MetaResultDimensionEntityAncestors))
        {
            final MetaResultDimensionEntityAncestors ancestorDimension = (MetaResultDimensionEntityAncestors) metaDimension;
            final IHierarchyBrowser editor = new TreeHierarchyBrowser(this.toolbox, rootEntities);
            editor.setEntityType(ancestorDimension.getEntityType());
            editor.setEmisMeta(this.emisMeta, this.hierarchy);
            editor.setPath(ancestorDimension.getEntityPath(), ancestorDimension.getEntityPathNames());
            editor.addValueChangeHandler(new ValueChangeHandler<EmisEntity>() {
                public void onValueChange(ValueChangeEvent<EmisEntity> event)
                {
                    ancestorDimension.setPath(editor.getPathIds(), editor.getPathNames(), editor.getDateIndex());
                    ValueChangeEvent.fire(MetaDimensionEditor.this, metaDimension);
                }
            });
            editor.getWidget().addStyleName("emisEdit");

            setTypeLabel(metaDimension);
            this.uiContainer.setWidget(editor.getWidget());
        }
        else
        {
            this.uiTypeContainer.setWidget(this.uiTypes);
            this.uiContainer.clear();
            ValueChangeEvent.fire(this, metaDimension);
        }
    }

    private void addDimensions(String sectionTitle, List<MetaResultDimension> dims)
    {
        if (dims == null || dims.size() == 0)
            return; 
        
        for (MetaResultDimension dim : dims)
        {
        	if (sectionTitle != null)
                uiTypes.add(sectionTitle, dim.getName(), dim); 
        	else
        		uiTypes.add(dim); 
        }
    }
    
    public void commit()
    {
        Widget w = this.uiContainer.getWidget();
        if ((w instanceof EnumTupleValueEditor))
        {
            MetaResultDimension metaDimension = (MetaResultDimension) this.uiTypes.getValue();
            if ((metaDimension instanceof MetaResultDimensionDate))
                ((MetaResultDimensionDate) metaDimension).setDateEnum(((EnumTupleValueEditor) w).get());
        }
    }

    public MetaResultDimension get()
    {
        commit();
        return (MetaResultDimension) this.uiTypes.getValue();
    }

    public void set(MetaResultDimension data)
    {
        int index = findMetaResultDimension(data);
        if (index == -1)
        {
            return;
        }
        this.uiTypes.setUserObject(index, data);
        this.uiTypes.setSelectedIndex(index);
        updateUi();
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<MetaResultDimension> handler)
    {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    private int findMetaResultDimension(MetaResultDimension dim)
    {
        for (int i = 0; i < this.uiTypes.getItemCount(); i++)
        {
            if (isSame(dim, (MetaResultDimension) this.uiTypes.getUserObject(i)))
            {
                return i;
            }
        }
        return -1;
    }

    private boolean isSame(MetaResultDimension dim1, MetaResultDimension dim2)
    {
        if (((dim1 instanceof MetaResultDimensionDate)) && ((dim2 instanceof MetaResultDimensionDate)))
            return isSame((MetaResultDimensionDate) dim1, (MetaResultDimensionDate) dim2);
        if (((dim1 instanceof MetaResultDimensionEntityAncestors)) && ((dim2 instanceof MetaResultDimensionEntityAncestors)))
            return isSame((MetaResultDimensionEntity) dim1, (MetaResultDimensionEntity) dim2);
        if (((dim1 instanceof MetaResultDimensionEntityChildren)) && ((dim2 instanceof MetaResultDimensionEntityChildren)))
            return isSame((MetaResultDimensionEntity) dim1, (MetaResultDimensionEntity) dim2);
        if (((dim1 instanceof MetaResultDimensionEntityGrandChildren)) && ((dim2 instanceof MetaResultDimensionEntityGrandChildren)))
            return isSame((MetaResultDimensionEntity) dim1, (MetaResultDimensionEntity) dim2);
        if (((dim1 instanceof MetaResultDimensionEnum)) && ((dim2 instanceof MetaResultDimensionEnum)))
        {
            return NamedUtil.sameName(((MetaResultDimensionEnum) dim1).getEnumType(), ((MetaResultDimensionEnum) dim2).getEnumType());
        }
        return false;
    }

    private boolean isSame(MetaResultDimensionDate dim1, MetaResultDimensionDate dim2)
    {
        return NamedUtil.sameName(dim1.getDateEnumType(), dim2.getDateEnumType());
    }

    private boolean isSame(MetaResultDimensionEntity dim1, MetaResultDimensionEntity dim2)
    {
        return NamedUtil.sameName(dim1.getEntityType(), dim2.getEntityType());
    }
}
