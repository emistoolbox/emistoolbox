package com.emistoolbox.client.ui.results;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.FieldListEditor;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.PriorityMetaResult;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

public class PriorityMetaResultEditor extends MetaResultEditor<PriorityMetaResult>
{
    private List<String> currentAdditionalFields = null; 

    public PriorityMetaResultEditor(EmisToolbox toolbox, EmisMeta emisMeta, EmisReportConfig reportConfig, List<EmisEntity> rootEntities) 
    {
        super(toolbox, emisMeta, reportConfig, rootEntities);
        currentAdditionalFields = new ArrayList<String>(); 
        currentAdditionalFields.add("name"); 
        updateUi();
    }

    protected PdfContentConfig getContentConfig(int addButton)
    { return null; }

    protected EmisEnumTupleValue getDefaultDate()
    { return null; }

    protected void updateUi()
    {
        clear(true);

        if (showCurrentHierarchy(0))
            return;

        if (showListEntity(2, getCurrentHierarchy().getEntityOrder()))
            return;

        if (showCurrentMetaResultValues(4, currentListEntity, Message.messageAdmin().mreHtmlVariables(), -1, false))
            return;

        if (showAdditionalFieldsEditor(6))
        	return; 

        if (showEntityEditor(8))
            return;

        if (showDateEditor(10, getUsedDateTypes())) 
            return;
        
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
