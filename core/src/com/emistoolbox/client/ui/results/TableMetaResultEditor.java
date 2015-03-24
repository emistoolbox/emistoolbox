package com.emistoolbox.client.ui.results;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.ui.DropDownSelect;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.impl.EnumTupleValueImpl;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfChartContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTableContentConfigImpl;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.TableMetaResult;
import com.emistoolbox.common.results.impl.MetaResultDimensionDate;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntity;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityFilter;
import com.emistoolbox.common.results.impl.MetaResultDimensionEnum;
import com.emistoolbox.common.results.impl.MetaResultUtil;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;

import java.util.List;
import java.util.Set;

public class TableMetaResultEditor extends MetaResultEditor<TableMetaResult>
{
    private MetaResultDimension currentXAxis = null;
    private MetaResultDimension currentYAxis = null;
    private boolean editingYAxis = false;
    private DropDownSelect uiChartType = new DropDownSelect();
    private Image uiChartTypeEdit = new Image("css/img/icon_edit.png"); 
    private DropDownSelect uiSortOrder = new DropDownSelect();
    private Image uiSortOrderEdit = new Image("css/img/icon_edit.png"); 

    public TableMetaResultEditor(EmisToolbox toolbox, EmisMeta emisMeta, EmisReportConfig config, List<EmisEntity> rootEntities) 
    {
        super(toolbox, emisMeta, config, rootEntities);

        uiChartTypeEdit.addClickHandler(uiChartType.getClickHandler()); 
        uiSortOrderEdit.addClickHandler(uiSortOrder.getClickHandler());

        this.uiChartType.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            public void onValueChange(ValueChangeEvent<Integer> event)
            {
                ValueChangeEvent.fire(TableMetaResultEditor.this, TableMetaResultEditor.this.get());
            }
        });

        this.uiSortOrder.addItem(Message.messageAdmin().mreSortNone(), "0");
        this.uiSortOrder.addItem(Message.messageAdmin().mreSortAscending(), "1");
        this.uiSortOrder.addItem(Message.messageAdmin().mreSortDescending(), "-1");
        this.uiSortOrder.addItem(Message.messageAdmin().mreSortName(), "2");

        this.uiSortOrder.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            public void onValueChange(ValueChangeEvent<Integer> event)
            {ValueChangeEvent.fire(TableMetaResultEditor.this, TableMetaResultEditor.this.get()); }
        });
        updateUi();
    }

    protected void updateUi()
    {
        clear(true);

        if (showCurrentHierarchy(0))
            return;

        if (showCurrentMetaResultValues(2, null, Message.messageAdmin().mreHtmlIndicator(), 1, true))
            return;

        setSectionHTML(4, 0, Message.messageAdmin().mreHtmlXAxis());
        if (this.currentXAxis == null)
        {
            editXAxis();
            return;
        }
        
        if (!isValidDimension(currentYAxis))
        	currentYAxis = null; 

        if (!isValidDimension(currentXAxis))
        {
        	currentXAxis = currentYAxis;
        	currentYAxis = null; 
        }
        
        show(5, this.currentXAxis, new ClickHandler() {
            public void onClick(ClickEvent event)
            { TableMetaResultEditor.this.editXAxis(); }
        });

        setSectionHTML(6, 0, Message.messageAdmin().mreHtmlSplitBy());
        if (this.editingYAxis)
        {
            editYAxis();
            return;
        }

        ClickHandler handler = new ClickHandler() {
            public void onClick(ClickEvent event)
            { TableMetaResultEditor.this.editYAxis(); }
        };
        if (this.currentYAxis == null)
        {
            show(7, Message.messageAdmin().mreHtmlNone(), handler);
        }
        else
        {
            show(7, this.currentYAxis, handler);
            HTML html = new HTML(Message.messageAdmin().mreHtmlSwitch());
            html.setStyleName("textButton");
            html.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event)
                {
                    if (TableMetaResultEditor.this.currentYAxis == null)
                        return;

                    MetaResultDimension tmp = TableMetaResultEditor.this.currentYAxis;
                    currentYAxis = currentXAxis;
                    currentXAxis = tmp;
                    updateUi();
                }
            });
            getCellFormatter().setHorizontalAlignment(8, 0, HasHorizontalAlignment.ALIGN_RIGHT);
            setWidget(8, 0, html);
        }

        if (needsEntityEditor() && showEntityEditor(9))
            return;
        
        if (showDateEditor(11, getUsedDateTypes(true)))
            return;

        setHTML(13, 0, "<p>");

        FlexTable links = new FlexTable();
        links.setStyleName("border");
        setWidget(14, 0, links);

        prepareChartType(getCurrentIndicator(), links);
        if (getCurrentIndicator() != null)
        {
            links.setHTML(1, 0, "<div class='section'>Sort:</div>");
            links.setWidget(1, 1, this.uiSortOrder);
            links.getFlexCellFormatter().setHorizontalAlignment(1, 2, HasHorizontalAlignment.ALIGN_RIGHT); 
            links.setWidget(1, 2, uiSortOrderEdit);
        }

        showAddToReport(links, 2, 1, new String[] { Message.messageAdmin().mreBtnAddChart(), Message.messageAdmin().mreBtnAddTable(), Message.messageAdmin().mreBtnAddChartTable() });
        ValueChangeEvent.fire(this, get());
    }

    private boolean isValidDimension(MetaResultDimension dim)
    {
    	if (dim instanceof MetaResultDimensionDate)
    		return getUsedDateTypes(false).contains(((MetaResultDimensionDate) dim).getDateEnumType());  
    	
    	return true; 
    }
    
    /** @return Date Enum that is required to calculate indicator. */
    public Set<EmisMetaDateEnum> getUsedDateTypes(boolean withoutAxises)
    { return MetaResultUtil.getUsedDateTypes(getCurrentMetaResultValues(), withoutAxises ? new MetaResultDimension[] { currentXAxis, currentYAxis } : null); }

    private boolean prepareChartType(EmisIndicator indicator, FlexTable links)
    {
        if (indicator == null)
        {
            this.uiChartType.setVisible(true);
            this.uiChartTypeEdit.setVisible(true); 
            return false; 
        }
        
        boolean addableResult = indicator.isAddableResult(getSplitEnum()); 
        links.setHTML(0, 0, "<div class='section'>" + Message.messageAdmin().mreHtmlChartStyle() + ":</div>");
        links.setWidget(0, 1, this.uiChartType);
        links.getFlexCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
        links.setWidget(0, 2, uiChartTypeEdit);

        this.uiChartType.clear();
        this.uiChartType.addItem(Message.messageAdmin().mreHtmlBarGraphs(), "0");
        if (addableResult && this.currentYAxis != null)
        {
            this.uiChartType.addItem(Message.messageAdmin().mreHtmlStackedBarChsrt(), "1");
            this.uiChartType.addItem(Message.messageAdmin().mreHtmlStackedBarChsrt() + " (100%)", "3");
        }

        this.uiChartType.addItem(Message.messageAdmin().mreHtmlLineChart(), "4");

        if (addableResult)
            this.uiChartType.addItem(Message.messageAdmin().mreHtmlPieChart(), "2");

        this.uiChartType.setVisible(true);
        this.uiChartType.setSelectedIndex(0);
        this.uiChartTypeEdit.setVisible(true);

        return true; 
    }
    
    private EmisMetaEnum getSplitEnum()
    {
        if (currentYAxis != null && currentYAxis instanceof MetaResultDimensionEnum)
            return ((MetaResultDimensionEnum) currentYAxis).getEnumType(); 
        
        return null; 
    }
    
    protected EmisEnumTupleValue getDefaultDate()
    {
        EmisMetaDateEnum dateEnum = getCurrentIndicator().getSeniorDateEnum();
        EmisMetaDateEnum defaultDateType = getEmisMeta().getDefaultDateType();
        if ((dateEnum != null) && (!NamedUtil.sameName(dateEnum, defaultDateType)))
        {
            return null;
        }
        EmisEnumTupleValue value = new EnumTupleValueImpl();
        value.setEnumTuple(defaultDateType);
        if ((this.currentXAxis instanceof MetaResultDimensionEntity))
        {
            value.setIndex(new byte[] { (byte) ((MetaResultDimensionEntity) this.currentXAxis).getHierarchyDateIndex() });
            return value;
        }
        if ((this.currentYAxis instanceof MetaResultDimensionEntity))
        {
            value.setIndex(new byte[] { (byte) ((MetaResultDimensionEntity) this.currentYAxis).getHierarchyDateIndex() });
            return value;
        }

        return null;
    }

    protected EmisMetaEntity getEntityType()
    {
        EmisMetaEntity entityType = super.getEntityType();
        if (entityType != null)
        {
            return entityType;
        }
        entityType = getEntityType(this.currentXAxis);
        if (entityType != null)
        {
            return entityType;
        }
        return getEntityType(this.currentYAxis);
    }

    private EmisMetaEntity getEntityType(MetaResultDimension dim)
    {
        if ((dim instanceof MetaResultDimensionEntity))
        {
            return ((MetaResultDimensionEntity) dim).getEntityType();
        }
        return null;
    }

    private boolean needsEntityEditor()
    { return (!(this.currentXAxis instanceof MetaResultDimensionEntity)) && (!(this.currentYAxis instanceof MetaResultDimensionEntity)); }
    
    private boolean needsDateEditor(Set<EmisMetaDateEnum> dateTypes)
    { return dateTypes != null && dateTypes.size() > 0; }

    private void editXAxis()
    {
        MetaDimensionEditor editor = new MetaDimensionEditor(getToolbox(), getEmisMeta(), getReportConfig(), getCurrentHierarchy(), getCurrentIndicator(), null, getRootEntities());
        editor.addValueChangeHandler(new ValueChangeHandler<MetaResultDimension>() {
            public void onValueChange(ValueChangeEvent<MetaResultDimension> event)
            {
                currentXAxis = event.getValue();
                if (((currentXAxis instanceof MetaResultDimensionEntity)) && ((currentYAxis instanceof MetaResultDimensionEntity)))
                    currentYAxis = null;
                if (((TableMetaResultEditor.this.currentXAxis instanceof MetaResultDimensionDate)) && ((TableMetaResultEditor.this.currentYAxis instanceof MetaResultDimensionDate)))
                    currentYAxis = null;
                if (((TableMetaResultEditor.this.currentXAxis instanceof MetaResultDimensionEnum)) && ((TableMetaResultEditor.this.currentYAxis instanceof MetaResultDimensionEnum)))
                {
                    if (NamedUtil.sameName(((MetaResultDimensionEnum) TableMetaResultEditor.this.currentXAxis).getEnumType(), ((MetaResultDimensionEnum) TableMetaResultEditor.this.currentYAxis).getEnumType()))
                        currentYAxis = null;
                }
                if (((TableMetaResultEditor.this.currentXAxis instanceof MetaResultDimensionEntityFilter)) && ((TableMetaResultEditor.this.currentYAxis instanceof MetaResultDimensionEntityFilter)))
                {
                    EmisMetaData fieldX = ((MetaResultDimensionEntityFilter) TableMetaResultEditor.this.currentXAxis).getField();
                    EmisMetaData fieldY = ((MetaResultDimensionEntityFilter) TableMetaResultEditor.this.currentYAxis).getField();
                    if ((NamedUtil.sameName(fieldX, fieldY)) && (NamedUtil.sameName(fieldX.getEntity(), fieldY.getEntity())))
                    {
                        currentYAxis = null;
                    }
                }
                TableMetaResultEditor.this.updateUi();
            }
        });
        setWidget(5, 0, editor);
        removeFromRow(6);
    }

    private void editYAxis()
    {
        this.editingYAxis = true;

        MetaDimensionEditor editor = new MetaDimensionEditor(getToolbox(), getEmisMeta(), getReportConfig(), getCurrentHierarchy(), getCurrentIndicator(), this.currentXAxis, getRootEntities());
        editor.addValueChangeHandler(new ValueChangeHandler<MetaResultDimension>() {
            public void onValueChange(ValueChangeEvent<MetaResultDimension> event)
            {
                currentYAxis = event.getValue();
                editingYAxis = false;
                TableMetaResultEditor.this.updateUi();
            }
        });
        setWidget(7, 0, editor);
        removeFromRow(8);
    }

    public void commit()
    {
        super.commit();

        if (this.currentYAxis == null)
        {
            ((TableMetaResult) this.metaResult).setDimensionCount(1);
            ((TableMetaResult) this.metaResult).setDimension(0, this.currentXAxis);
        }
        else
        {
            ((TableMetaResult) this.metaResult).setDimensionCount(2);
            ((TableMetaResult) this.metaResult).setDimension(0, this.currentXAxis);
            ((TableMetaResult) this.metaResult).setDimension(1, this.currentYAxis);
        }

        ((TableMetaResult) this.metaResult).setContext(getContext(needsEntityEditor(), getUsedDateTypes(false)));
        ((TableMetaResult) this.metaResult).setSortOrder(getSortOrder());
    }

    private int getSortOrder()
    {
        int index = this.uiSortOrder.getSelectedIndex();
        if (index == -1)
        {
            return 0;
        }
        return Integer.parseInt(this.uiSortOrder.getValue(index));
    }

    public void set(TableMetaResult metaResult)
    {
        super.set(metaResult);

        this.currentXAxis = (metaResult.getDimensionCount() > 0 ? metaResult.getDimension(0) : null);
        this.currentYAxis = (metaResult.getDimensionCount() > 1 ? metaResult.getDimension(1) : null);

        switch (metaResult.getSortOrder()) {
        case -1:
            this.uiSortOrder.setSelectedIndex(2);
            break;
        case 0:
            this.uiSortOrder.setSelectedIndex(0);
            break;
        case 1:
            this.uiSortOrder.setSelectedIndex(1);
            break;
        case 2:
            this.uiSortOrder.setSelectedIndex(3);
        }
    }

    public int getChartType()
    {
        if ((!this.uiChartType.isVisible()) || (this.uiChartType.getSelectedIndex() == -1))
            return 0;

        return Integer.parseInt(this.uiChartType.getValue(this.uiChartType.getSelectedIndex()));
    }

    public void setChartType(int chartType)
    {
        this.uiChartType.setSelectedIndex(chartType);
    }

    public void setCurrentHierarchy(EmisMetaHierarchy currentHierarchy)
    {
        super.setCurrentHierarchy(currentHierarchy);
        if ((this.currentXAxis instanceof MetaResultDimensionEntity))
            this.currentXAxis = null;
        if ((this.currentYAxis instanceof MetaResultDimensionEntity))
            this.currentYAxis = null;
    }

    protected PdfContentConfig getContentConfig(int addButton)
    {
        if (addButton == 0)
        {
            PdfChartContentConfigImpl result = new PdfChartContentConfigImpl();
            result.setChartType(getChartType()); 
            result.setMetaResult(get());
            return result;
        }

        PdfTableContentConfigImpl result = new PdfTableContentConfigImpl();
        result.setMetaResult(get());
        return result;
    }

    private EmisIndicator getCurrentIndicator()
    {
        List<MetaResultValue> values = getCurrentMetaResultValues();
        if ((values == null) || (values.size() != 1))
            return null;

        return values.get(0).getIndicator();
    }
}
