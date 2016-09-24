package com.emistoolbox.client.ui.results;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.DateTupleEditor;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.client.admin.ui.MetaResultValueListEditor;
import com.emistoolbox.client.admin.ui.TreeHierarchyBrowser;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.analysis.impl.Context;
import com.emistoolbox.common.model.impl.EmisEnumUtils;
import com.emistoolbox.common.model.impl.EnumTupleValueImpl;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfReportConfigImpl;
import com.emistoolbox.common.results.ListEntityMetaResult;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.impl.MetaResultUtil;
import com.emistoolbox.common.user.EmisUser.AccessLevel;
import com.emistoolbox.common.util.Named;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfUtil;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class MetaResultEditor<T extends MetaResult> extends FlexTable implements EmisEditor<T>, HasValueChangeHandlers<T>
{
    private EmisToolbox toolbox;
    protected EmisMeta emisMeta;
    private EmisReportConfig reportConfig;
    protected T metaResult = null;
    protected EmisMetaHierarchy currentHierarchy;
    private List<MetaResultValue> currentMetaResultValues;
    private EmisEntity currentEntity;
    private int[] entityPathIds;
    private String[] entityPathNames;
    private EmisEnumTupleValue currentDate;
    private int currentHierarchyDateIndex = -1;
    protected EmisMetaEntity currentListEntity;
    private EmisContext globalFilter; 

    private List<EmisEntity> rootEntities; 
    
    private HasValueChangeHandlers<PdfReportConfig> showReportHasHandlers = new HasValueChangeHandlers<PdfReportConfig>() {
        private HandlerManager showReportHandlerManager = new HandlerManager(MetaResultEditor.this);

        public HandlerRegistration addValueChangeHandler(ValueChangeHandler<PdfReportConfig> handler)
        { return this.showReportHandlerManager.addHandler(ValueChangeEvent.getType(), handler); }

        public void fireEvent(GwtEvent<?> event)
        { this.showReportHandlerManager.fireEvent(event); }
    };

    public MetaResultEditor(EmisToolbox toolbox, EmisMeta emisMeta, EmisReportConfig reportConfig, List<EmisEntity> rootEntities) 
    {
        this.toolbox = toolbox;
        this.emisMeta = emisMeta;
        this.reportConfig = reportConfig;
        this.rootEntities = rootEntities; 

        setCurrentHierarchy((EmisMetaHierarchy) emisMeta.getHierarchies().get(0));
    }

    public void setGlobalFilter(EmisContext context)
    {
    	globalFilter = context;
    	EmisEnumTupleValue dt = getCurrentDate();
    	if (dt != null)
    	{
	    	dt = filterDate(dt); 
	    	if (dt == null)
	    		setCurrentDate(null);     	
    	}
    	
    	updateUi();
    }
    
    
    public EmisContext getGlobalFilter()
    { return globalFilter; } 
    
    public List<EmisEntity> getRootEntities()
    { return rootEntities; }
    
    public HandlerRegistration addShowReportHandler(ValueChangeHandler<PdfReportConfig> showReportHandler)
    {
        return this.showReportHasHandlers.addValueChangeHandler(showReportHandler);
    }

	protected abstract void updateUi();
    
    protected EmisEnumTupleValue getDefaultDate(Set<EmisMetaDateEnum> dateTypes)
    {
    	if (dateTypes == null || dateTypes.size() == 0)
    		return null; 
    	
    	EmisMetaDateEnum defaultDateType = getEmisMeta().getDefaultDateType();

    	EmisMetaDateEnum bestDateType = null; 
    	for (EmisMetaDateEnum dateType : dateTypes)
    	{
    		if (NamedUtil.sameName(dateType, defaultDateType))
    		{
                EmisEnumTupleValue value = new EnumTupleValueImpl();
                value.setEnumTuple(dateType);
                value.setIndex(new byte[] { (byte) getEmisMeta().getDefaultDateTypeIndex() });
                
                return value; 
    		}
    		
    		if (bestDateType == null || bestDateType.getDimensions() > dateType.getDimensions())
    			bestDateType = dateType; 
    	}
    	
    	if (bestDateType == null)
    		return null; 
    	
        EmisEnumTupleValue value = new EnumTupleValueImpl();
        value.setEnumTuple(bestDateType);
        byte[] index = new byte[bestDateType.getDimensions()]; 
        for (int i = 0; i < index.length; i++) 
        {
        	if (NamedUtil.sameName(defaultDateType, bestDateType.getEnums()[i]))
        		index[i] = (byte) getEmisMeta().getDefaultDateTypeIndex(); 
        	else
        		index[i] = 0; 
        }

        value.setIndex(index);
    	
    	return value; 
    }

    public void commit()
    {
        this.metaResult.setHierarchy(getCurrentHierarchy());
        this.metaResult.setMetaResultValues(getCurrentMetaResultValues());
        if (metaResult instanceof ListEntityMetaResult) 
        	((ListEntityMetaResult) metaResult).setListEntity(currentListEntity);
    }

    public EmisContext getContext(boolean needsEntityEditor, Set<EmisMetaDateEnum> dateTypes)
    {
    	EmisContext result = getContext(true, false); 
    	if (getCurrentDate() == null)
    		return result; 
 
    	EmisMetaDateEnum targetDateType = EmisEnumUtils.findLowestEnum(dateTypes); 
    	
    	EmisEnumTupleValue currentDate = getCurrentDate();
    	byte[] currentIndexes = currentDate.getIndex(); 

    	EmisEnumTupleValue date = new EnumTupleValueImpl(); 
        date.setEnumTuple(targetDateType);

    	EmisMetaEnum[] enums = targetDateType.getEnums();
    	byte[] indexes = new byte[enums.length]; 
    	for (int i = 0; i < enums.length; i++) 
    	{
    		int pos = currentDate.getEnumTuple().findEnumPosition(enums[i]); 
    		if (pos == -1)
    			indexes[i] = 0; // Place holder value - the dimension will override this.
    		else
    			indexes[i] = currentIndexes[pos]; 
    	}
    	date.setIndex(indexes); 
    	
    	List<EmisEnumTupleValue> newDates = new ArrayList<EmisEnumTupleValue>(); 
    	newDates.add(date); 
    	result.setDateType(targetDateType);
    	result.setDates(newDates); 

    	return result; 
    }
    
    /** @deprecated */ 
    public EmisContext getContext(boolean needsEntityEditor, boolean needsDateEditor)
    {
        EmisContext context = new Context();
        if (getCurrentEntity() != null && needsEntityEditor)
        {
            List<EmisEntity> entities = new ArrayList<EmisEntity>();
            context.setEntityType(getCurrentEntity().getEntityType());
            entities.add(getCurrentEntity());
            context.setEntities(entities);
            context.setHierarchyDateIndex(getCurrentHierarchyDateIndex());
        }

        if (getCurrentDate() != null && needsDateEditor)
        {
            List<EmisEnumTupleValue> dates = new ArrayList<EmisEnumTupleValue>();
            context.setDateType((EmisMetaDateEnum) getCurrentDate().getEnumTuple());
            EmisEnumTupleValue tmpDate = getCurrentDate();
            dates.add(tmpDate);
            context.setDates(dates);
            context.setDateType((EmisMetaDateEnum) tmpDate.getEnumTuple());
        }

        return context;
    }
    
    public T get()
    {
        commit();
        return this.metaResult;
    }

    public void set(T metaResult)
    {
        this.metaResult = metaResult;
        setCurrentHierarchy(metaResult.getHierarchy());
        if (getCurrentHierarchy() == null)
            setCurrentHierarchy((EmisMetaHierarchy) getEmisMeta().getHierarchies().get(0));

        setCurrentMetaResultValues(metaResult.getMetaResultValues());
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler)
    { return addHandler(handler, ValueChangeEvent.getType()); }

    public EmisToolbox getToolbox()
    { return this.toolbox; }

    public void setToolbox(EmisToolbox toolbox)
    { this.toolbox = toolbox; }

    public EmisMeta getEmisMeta()
    { return this.emisMeta; }

    public void setEmisMeta(EmisMeta emisMeta)
    { this.emisMeta = emisMeta; }

    public EmisReportConfig getReportConfig()
    { return this.reportConfig; }

    public void setReportConfig(EmisReportConfig reportConfig)
    {
        this.reportConfig = reportConfig;
    }

    public T getMetaResult()
    {
        return this.metaResult;
    }

    public void setMetaResult(T metaResult)
    {
        this.metaResult = metaResult;
    }

    public EmisMetaHierarchy getCurrentHierarchy()
    {
        return this.currentHierarchy;
    }

    public void setCurrentHierarchy(EmisMetaHierarchy currentHierarchy)
    {
        this.currentHierarchy = currentHierarchy;
    }

    public List<MetaResultValue> getCurrentMetaResultValues()
    {
        return this.currentMetaResultValues;
    }

    public void setCurrentMetaResultValues(List<MetaResultValue> values)
    {
        this.currentMetaResultValues = values;
    }

    public EmisEntity getCurrentEntity()
    { return this.currentEntity; }

    public void setCurrentEntity(EmisEntity currentEntity)
    {
        this.currentEntity = currentEntity;
    }

    public void setCurrentEntityPath(int[] ids, String[] names)
    {
        this.entityPathIds = ids;
        this.entityPathNames = names;
    }

    public int[] getEntityPathIds()
    {
        return this.entityPathIds;
    }

    public String[] getEntityPathNames()
    {
        return this.entityPathNames;
    }

    public EmisEnumTupleValue getCurrentDate()
    {
        return this.currentDate;
    }

    public void setCurrentDate(EmisEnumTupleValue currentDate)
    {
        this.currentDate = currentDate;
    }

    public int getCurrentHierarchyDateIndex()
    {
        return this.currentHierarchyDateIndex;
    }

    public void setCurrentHierarchyDateIndex(int currentHierarchyDateIndex)
    {
        this.currentHierarchyDateIndex = currentHierarchyDateIndex;
    }

    protected boolean showCurrentHierarchy(final int row)
    {
        setSectionHTML(row, 0, Message.messageAdmin().mreHtmlHierarchy());
        if (this.currentHierarchy == null)
        {
            editHierarchy(row);
            return true;
        }

        show(row + 1, this.currentHierarchy, new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                MetaResultEditor.this.editHierarchy(row);
            }
        });
        return false;
    }

    protected boolean showCurrentMetaResultValues(final int row, final EmisMetaEntity forEntity, String title, final int listSize, final boolean indicatorOnly)
    {
        setSectionHTML(row, 0, title);
        if (needsEdit(this.currentMetaResultValues, listSize))
        {
            editMetaResultValues(row, forEntity, listSize, indicatorOnly);
            return true;
        }

        show(row + 1, getHTML(this.currentMetaResultValues), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                MetaResultEditor.this.editMetaResultValues(row, forEntity, listSize, indicatorOnly);
            }
        });
        return false;
    }
    
    private boolean needsEdit(List<MetaResultValue> metaValues, int listSize)
    {
    	if (metaValues == null || metaValues.size() == 0)
    		return true;
    	
    	if (listSize > 0 && metaValues.size() != listSize)
    		return true; 
    	
    	if (metaValues.size() == 1)
    	{
    		MetaResultValue value = metaValues.get(0); 
    		if (value.getIndicator() == null)
    			return true; 
    	}

    	return false; 
    }

    private String getHTML(List<MetaResultValue> values)
    {
        StringBuffer result = new StringBuffer();
        String seperator = "";
        for (MetaResultValue value : values)
        {
            if (value.getIndicator() == null)
            {
                continue;
            }
            result.append(seperator);
            seperator = "<br>";
            result.append(value.getName(true));
        }

        return result.toString();
    }

    protected boolean showEntityEditor(final int row)
    {
        setSectionHTML(row, 0, Message.messageAdmin().mreHtmlEntity());
        if (this.currentEntity == null)
        {
            editEntity(row);
            return true;
        }

        show(row + 1, this.currentEntity.getName(), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                MetaResultEditor.this.editEntity(row);
            }
        });
        return false;
    }

    private boolean validDate(EmisEnumTupleValue dt, Set<EmisMetaDateEnum> dateTypes)
    {
    	byte[] indexes = dt.getIndex();
    	for (EmisMetaDateEnum dateType : dateTypes)
    	{
    		int pos = dt.getEnumTuple().findEnumPosition(dateType); 
    		if (pos == -1 || indexes[pos] == -1)
    			return false; 
    	}
    	
    	return true; 
    }

    protected boolean showDateEditor(final int row, Set<EmisMetaDateEnum> dateTypes)
    {
    	if (dateTypes == null)
    	{
    		dateTypes = new HashSet<EmisMetaDateEnum>(); 
    		dateTypes.add(getEmisMeta().getDefaultDateType()); 
    	}
    	else if (dateTypes.size() == 0)
    	{ return false; }
    	
        setSectionHTML(row, 0, Message.messageAdmin().mreHtmlDate());
        if (this.currentDate == null)
            this.currentDate = getDefaultDate(dateTypes);
        else
        	this.currentDate = currentDate.get(dateTypes);  

        if (this.currentDate == null || !validDate(currentDate, dateTypes))
        {
            editDate(row, dateTypes);
            return true;
        }

        String dateString = getDateString(currentDate, dateTypes); 
        if ((dateString == null) || (dateString.equals("")))
            dateString = "(none)";

        final Set<EmisMetaDateEnum> dateTypesFinal = dateTypes; 
        show(row + 1, dateString, new ClickHandler() {
            public void onClick(ClickEvent event)
            { MetaResultEditor.this.editDate(row, dateTypesFinal); }
        });
        
        return false;
    }
    
    protected String getDateString(EmisEnumTupleValue dt, Set<EmisMetaDateEnum> dateTypes)
    {
    	if (dt == null)
    		return ""; 

    	StringBuffer result = new StringBuffer();
    	byte[] indexes = dt.getIndex();
    	EmisMetaEnum[] enums = dt.getEnumTuple().getEnums(); 
    	
    	String delim = ""; 
    	for (int i = 0; i < indexes.length; i++)
    	{
    		if (indexes[i] == -1 || !dateTypes.contains((EmisMetaDateEnum) enums[i]) )
    			continue;

    		result.append(delim); 
    		result.append(enums[i].getValue(indexes[i]));
    		delim = " "; 
    	}

    	return result.toString();
    }

    protected void showAddToReport(FlexTable table, int row, int col, final String[] addButtons)
    {
        if (toolbox.getCurrentUser() != null && toolbox.getCurrentUser().getAccessLevel() == AccessLevel.VIEWER)
            return; 
        
        final HTML html = new HTML("<nobr>[" + Message.messageAdmin().mreHtmlAddToReport() + "]</nobr>");
        html.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { MetaResultEditor.this.addReport(addButtons, html); }
        });
        html.addStyleName("pointer");
        table.setWidget(row, col, html);
    }

    private void addReport(String[] addButtons, Widget anchor)
    {
        final PopupPanel popup = new PopupPanel();
        popup.setAutoHideEnabled(true);
        popup.setModal(true);

        VerticalPanel root = new VerticalPanel();
        root.setSpacing(2);

        popup.setWidget(root);

        root.add(new Label("Please select the report you want to add to:"));

        List<EmisPdfReportConfig> reports = getReportConfig().getPdfReports();

        final ListBoxWithUserObjects<EmisPdfReportConfig> uiReports = new ListBoxWithUserObjects<EmisPdfReportConfig>();
        uiReports.add(Message.messageAdmin().mreHtmlCreateNewReport(), null);

        PdfContentConfig contentConfig = getContentConfig(0);
        for (EmisPdfReportConfig report : reports)
        {
            if (report.allowContentConfig(contentConfig))
                uiReports.add(report.getText(PdfText.TEXT_TITLE) + (report.getEntityType() != null ? "(" + report.getEntityType().getName() + ")" : ""), report);
        }
        HorizontalPanel hp = new HorizontalPanel();
        if (addButtons.length == 1)
            hp.add(uiReports);

        for (int i = 0; i < addButtons.length; i++)
        {
            final int buttonIndex = i;

            PushButton btnAdd = new PushButton(addButtons[i], new ClickHandler() {
                public void onClick(ClickEvent event)
                {
                    PdfReportConfig report = (PdfReportConfig) uiReports.getValue();
                    if (report == null)
                    {
                        String newId = EmisUtils.getUniqueId(NamedUtil.getNames(MetaResultEditor.this.getReportConfig().getPdfReports()), Message.messageAdmin().prcleNewReportId());
                        if (newId == null)
                            return;
                        report = new PdfReportConfigImpl();
                        report.setName(newId);
                        MetaResultEditor.this.getReportConfig().getPdfReports().add(report);

                        uiReports.add(report.getText(PdfText.TEXT_TITLE), report);
                    }

                    popup.hide();

                    if (buttonIndex == 3)
                    {
                        report.addContentConfig(MetaResultEditor.this.getContentConfig(1), MetaResultEditor.this.getEntityType());
                        report.addContentConfig(MetaResultEditor.this.getContentConfig(2), MetaResultEditor.this.getEntityType());
                    }
                    else
                        report.addContentConfig(MetaResultEditor.this.getContentConfig(buttonIndex), MetaResultEditor.this.getEntityType());

                    ValueChangeEvent.fire(MetaResultEditor.this.showReportHasHandlers, report);
                }
            });
            EmisUtils.init(btnAdd, 80);
            hp.add(btnAdd);
        }

        root.add(uiReports);
        root.add(new HTML("&nbsp;"));
        root.add(hp);

        popup.setGlassEnabled(true);
        popup.showRelativeTo(anchor);
    }

    protected abstract PdfContentConfig getContentConfig(int paramInt);

    private void editHierarchy(int row)
    {
        if (getEmisMeta().getHierarchies().size() < 2)
        {
            setCurrentHierarchy((EmisMetaHierarchy) getEmisMeta().getHierarchies().get(0));
            return;
        }

        final ListBoxWithUserObjects<EmisMetaHierarchy> uiHierarchies = new ListBoxWithUserObjects<EmisMetaHierarchy>();
        uiHierarchies.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                if (uiHierarchies.getValue() != null)
                {
                    setCurrentHierarchy((EmisMetaHierarchy) uiHierarchies.getValue());
                    setCurrentEntity(null);
                    setCurrentEntityPath(null, null);

                    updateUi();
                }
            }
        });
        uiHierarchies.add("", null);
        for (EmisMetaHierarchy hierarchy : getEmisMeta().getHierarchies())
        {
            uiHierarchies.add(hierarchy.getName(), hierarchy);
        }
        setWidget(row + 1, 0, uiHierarchies);
        removeFromRow(row + 2);
    }

    private void editMetaResultValues(int row, EmisMetaEntity forEntity, int listSize, boolean indicatorOnly)
    {
        MetaResultValueListEditor editor = new MetaResultValueListEditor(getReportConfig().getIndicators(), listSize, indicatorOnly);
        editor.set(this.currentMetaResultValues);
        editor.addValueChangeHandler(new ValueChangeHandler<List<MetaResultValue>>() {
            public void onValueChange(ValueChangeEvent<List<MetaResultValue>> event)
            {
                MetaResultEditor.this.setCurrentMetaResultValues(event.getValue());
                MetaResultEditor.this.updateUi();
            }
        });
        setWidget(row + 1, 0, editor);
        removeFromRow(row + 2);
    }

    private IHierarchyBrowser getHierarchyBrowser()
    { return new TreeHierarchyBrowser(getToolbox(), rootEntities); }

    private void editEntity(int row)
    {
        final IHierarchyBrowser editor = getHierarchyBrowser();
        configureEntityEditor(editor);
        editor.setEmisMeta(getEmisMeta(), getCurrentHierarchy());
        editor.addValueChangeHandler(new ValueChangeHandler<EmisEntity>() {
            public void onValueChange(ValueChangeEvent<EmisEntity> event)
            {
                MetaResultEditor.this.setCurrentEntity((EmisEntity) event.getValue());
                MetaResultEditor.this.setCurrentEntityPath(editor.getPathIds(), editor.getPathNames());
                currentHierarchyDateIndex = editor.getDateIndex();
                MetaResultEditor.this.updateUi();
            }
        });
        editor.getWidget().addStyleName("emisEdit");
        setWidget(row + 1, 0, editor.getWidget());
        removeFromRow(row + 2);
    }

    private int getCurrentDateIndex()
    {
    	EmisEnumTupleValue value = getCurrentDate();
    	if (value == null)
    		return 0; 
    	
    	byte[] indexes = value.getIndex();
    	return (indexes == null || indexes.length == 0) ? 0 : indexes[0];  
    }
    
    protected void configureEntityEditor(IHierarchyBrowser editor)
    {
    	editor.setAnySelection(true);
    	editor.setDateIndex(getCurrentDateIndex()); 
    }

    private EmisEnumTupleValue filterDate(EmisEnumTupleValue dt)
    {
    	if (globalFilter == null)
    		return dt; 
    	
    	EmisMetaEnum[] enumTypes = dt.getEnumTuple().getEnums();
    	String[] values = dt.getValue(); 
    	
    	for (int i = 0; i < enumTypes.length; i++) 
    	{
    		EmisEnumSet filter = globalFilter.getDateEnumFilter(enumTypes[i].getName());
    		if (filter != null && !filter.hasValue(values[i]))
    			return null;  
    	}
    	
    	return dt; 
    }
    
    private void editDate(int row, Set<EmisMetaDateEnum> dateTypes)
    {
        DateTupleEditor editor = new DateTupleEditor(dateTypes, globalFilter);
        editor.set(getCurrentDate());
        editor.addValueChangeHandler(new ValueChangeHandler<EmisEnumTupleValue>() {
            public void onValueChange(ValueChangeEvent<EmisEnumTupleValue> event)
            {
                MetaResultEditor.this.setCurrentDate((EmisEnumTupleValue) event.getValue());
                MetaResultEditor.this.updateUi();
            }
        });
        setWidget(row + 1, 0, editor);
        removeFromRow(row + 2);
    }

    protected void removeFromRow(int row)
    {
        while (getRowCount() > row)
            removeRow(row);
    }

    protected void show(int row, Named item, ClickHandler handler)
    {
        show(row, item.getName(), handler);
    }

    String htmlEdit = "<span class='right'><img src='css/img/icon_edit.png' alt='Edit' width='22' height='19'></span>"; 
    
    protected void show(int row, String name, ClickHandler handler)
    {
        HTML html = new HTML(htmlEdit + "<b>" + name + "</b>");
        html.addClickHandler(handler);
        html.addStyleName("pointer");
        setWidget(row, 0, html);
    }

    protected EmisMetaDateEnum getSeniorDateEnum()
    {
        List<MetaResultValue> values = getCurrentMetaResultValues();
        if ((values == null) || (values.size() == 0))
        {
            return null;
        }
        EmisMetaDateEnum result = null;
        for (MetaResultValue value : values)
        {
            if (value.getIndicator() == null)
                continue;

            EmisMetaDateEnum tmp = value.getIndicator().getSeniorDateEnum();
            if (tmp == null)
                continue;

            if ((result == null) || (result.getDimensions() > tmp.getDimensions()))
                result = tmp;
        }

        return result;
    }

    protected EmisMetaEntity getEntityType()
    {
        if (getCurrentEntity() != null)
            return getCurrentEntity().getEntityType();
        return null;
    }
    
    public void setSectionHTML(int row, int col, String text)
    {
    	String[] styles = row > 0 ? new String[] { EmisToolbox.CSS_SECTION, EmisToolbox.CSS_BORDER } : new String[] { EmisToolbox.CSS_SECTION }; 
    	setHTML(row, col, EmisToolbox.div(styles, text)); 
    }

    protected boolean showListEntity(final int row, final List<EmisMetaEntity> entityTypes)
    {
    	if (entityTypes.size() == 1)
    		currentListEntity = entityTypes.get(0); 
    	
        setSectionHTML(row, 0, Message.messageAdmin().mreHtmlListEntity());
        if (this.currentListEntity == null)
        {
            editListEntity(row, entityTypes);
            return true;
        }

        show(row + 1, this.currentListEntity, new ClickHandler() {
            public void onClick(ClickEvent event)
            { editListEntity(row, entityTypes); }
        });
        
        return false;
    }

    protected void editListEntity(int row, List<EmisMetaEntity> entityTypes)
    {
        final ListBoxWithUserObjects<EmisMetaEntity> uiEntityTypes = new ListBoxWithUserObjects<EmisMetaEntity>();
        
        uiEntityTypes.clear();
        uiEntityTypes.addItem("", (EmisMetaEntity) null);
        for (EmisMetaEntity entityType : entityTypes)
            uiEntityTypes.addItem(entityType.getName(), entityType);

        uiEntityTypes.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                setCurrentListEntity((EmisMetaEntity) uiEntityTypes.getUserObject());
                updateUi();
            }
        });
        setWidget(row + 1, 0, uiEntityTypes);
        removeFromRow(row + 2);
    }

    public void setCurrentListEntity(EmisMetaEntity newListEntity)
    { this.currentListEntity = newListEntity; }

    protected Set<EmisMetaDateEnum> getUsedDateTypes()
    { return MetaResultUtil.getUsedDateTypes(getCurrentMetaResultValues(), null); }
}
