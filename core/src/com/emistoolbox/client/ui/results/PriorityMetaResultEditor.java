package com.emistoolbox.client.ui.results;

import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.FieldListEditor;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.priolist.PriorityReportConfig;
import com.emistoolbox.common.model.priolist.impl.PriorityReportConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.results.PriorityMetaResult;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;

public class PriorityMetaResultEditor extends MetaResultEditor<PriorityMetaResult>
{
    private List<String> currentAdditionalFields = null; 
    private PushButton btnSave = new PushButton("Save Priority List"); 
    private ListBoxWithUserObjects<PriorityReportConfig> uiPriorityReports = new ListBoxWithUserObjects<PriorityReportConfig>(); 
    
    public PriorityMetaResultEditor(EmisToolbox toolbox, EmisMeta emisMeta, EmisReportConfig reportConfig, List<EmisEntity> rootEntities) 
    {
        super(toolbox, emisMeta, reportConfig, rootEntities);
        currentAdditionalFields = new ArrayList<String>(); 
        currentAdditionalFields.add("name"); 
        
        EmisUtils.init(btnSave, 120); 
        btnSave.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String id = EmisUtils.getUniqueId(getReportConfig().getPriorityReports(), "Please enter the name of your Priority List"); 
				if (id == null)
					return; 
				
				PriorityReportConfig result = new PriorityReportConfigImpl(); 
				result.setName(id);
				result.setMetaResult(get());
				
				getReportConfig().getPriorityReports().add(result);
				updatePriorityReportList();
			}
        }); 
        
		updatePriorityReportList();
        uiPriorityReports.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				PriorityReportConfig config = uiPriorityReports.getUserObject(); 
				if (config != null)
				{
					set(config.getMetaResult()); 
					updateUi(); 
				}
			}
        }); 
        
        updateUi();
    }

    private void updatePriorityReportList()
    {
    	uiPriorityReports.clear(); 
		uiPriorityReports.add("(none)", null); 
    	for (PriorityReportConfig report : getReportConfig().getPriorityReports())
    		uiPriorityReports.add(report);
    	
    }
    
    protected PdfContentConfig getContentConfig(int addButton)
    { return null; }

    protected EmisEnumTupleValue getDefaultDate()
    { return null; }

    protected void updateUi()
    {
        clear(true);

        int row = 0; 
        HorizontalPanel hp = new HorizontalPanel(); 
        hp.setWidth("100%");
        hp.add(new HTML("<b>Load Priority List:</b>")); 
        hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        hp.add(uiPriorityReports);
        
        setWidget(row, 0, hp); 
        row++; 
        
        if (showCurrentHierarchy(row))
            return;
        row += 2; 

        if (showListEntity(row, getCurrentHierarchy().getEntityOrder()))
            return;
        row += 2; 

        if (showCurrentMetaResultValues(row, currentListEntity, Message.messageAdmin().mreHtmlVariables(), -1, false))
            return;
        row += 2; 

        if (showAdditionalFieldsEditor(row))
        	return; 
        row += 2; 

        if (showEntityEditor(row))
            return;
        row += 2; 

        if (showDateEditor(row, getUsedDateTypes())) 
            return;
        row += 2; 

        // Additional controls once MetaResult has been specified. 
        // 
        FlexTable links = new FlexTable();
        links.setStyleName("border");
        setWidget(row, 0, links);

        links.setWidget(0, 0, btnSave); 
        showAddToReport(links, 2, 1, new String[] { "Add to PDF Report" });

        ValueChangeEvent.fire(this, get());
    }
    
    protected boolean showAdditionalFieldsEditor(final int row)
    {
        setSectionHTML(row, 0, "Additional Fields"); // i18n
        if (this.currentAdditionalFields == null)
        {
            editAdditionalFields(row);
            return true;
        }

        show(row + 1, getFieldList(this.currentAdditionalFields), new ClickHandler() {
            public void onClick(ClickEvent event)
            { editAdditionalFields(row); }
        });
        
        return false;
    }
    
    private String getFieldList(List<String> fields)
    {
    	StringBuffer result = new StringBuffer(); 
    	for (String field : fields)
    	{
    		if (result.length() > 0)
    			result.append(","); 
    		result.append(field); 
    	}
    	
    	return result.toString(); 
    }
    
    private void editAdditionalFields(final int row)
    {
    	final FieldListEditor editor = new FieldListEditor(currentListEntity);
    	if (currentAdditionalFields == null)
    	{
    		List<String> tmpFields = new ArrayList<String>();
    		tmpFields.add("name");
    		editor.set(tmpFields);
    	}
    	else
    		editor.set(currentAdditionalFields);
    		
    	editor.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
			@Override
			public void onValueChange(ValueChangeEvent<List<String>> event)
			{
				currentAdditionalFields = event.getValue();
				updateUi(); 
			}
    	});
    	
    	setWidget(row + 1, 0, editor); 
        removeFromRow(row + 2);
    }

    public EmisMetaEntity getCurrentListEntity()
    { return this.currentListEntity; }

    public void commit()
    {
        super.commit();
        ((PriorityMetaResult) getMetaResult()).setListEntity(this.currentListEntity);

        EmisContext context = getContext(true, true);
        context.setEntityType(this.currentListEntity);
        ((PriorityMetaResult) this.metaResult).setContext(context);
        
        ((PriorityMetaResult) this.metaResult).setAdditionalFields(currentAdditionalFields.toArray(new String[0]));
    }
}
