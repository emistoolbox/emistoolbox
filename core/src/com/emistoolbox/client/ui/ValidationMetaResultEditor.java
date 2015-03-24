package com.emistoolbox.client.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.StatusAsyncCallback;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.client.ui.results.MetaResultEditor;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.model.validation.EmisValidationRule;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.results.ValidationMetaResult;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ValidationMetaResultEditor extends MetaResultEditor<ValidationMetaResult> 
{
	private VerticalPanel vp = new VerticalPanel(); 
	private ListBoxWithUserObjects<EmisValidation> uiValidations = null;  
	private PushButton btnOk = null;

	private List<String> currentValidationIds = null; 
	private List<EmisValidation> validations; 
			
	public ValidationMetaResultEditor(EmisToolbox toolbox, EmisMeta emisMeta, EmisReportConfig reportConfig, List<EmisEntity> rootEntities)
	{
		super(toolbox, emisMeta, reportConfig, rootEntities); 
		
		toolbox.getService().loadValidations(emisMeta.getDatasetName(), new StatusAsyncCallback<List<EmisValidation>>("Loading validation rules") {

			@Override
			public void onFailure(Throwable caught) 
			{ super.onFailure(caught); }

			@Override
			public void onSuccess(List<EmisValidation> result) 
			{
				super.onSuccess(result);
				validations = result; 
				
				if (result == null || result.size() == 0)
				{
			        clear(true);
			        setSectionHTML(0, 0, "No validations available!"); // TODO 
					return;
				}
				
				uiValidations = new ListBoxWithUserObjects<EmisValidation>();		        
				uiValidations.add("", null);
				for (EmisValidation validation : result)
					uiValidations.add(validation.getId(), validation); 

				vp.add(uiValidations);
				vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
				vp.add(btnOk);

				updateUi();
			}
		}); 

		btnOk = new PushButton("OK", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{
				currentValidationIds = getValidationIds();
				updateUi();
			}
		}); 
		EmisUtils.init(btnOk, 80); 
	}
	
	public List<EmisValidation> getSelectedValidations()
	{
		List<EmisValidation> result = new ArrayList<EmisValidation>(); 
		for (int i = 0; i < uiValidations.getItemCount(); i++)
		{
			if (uiValidations.isItemSelected(i) && uiValidations.getUserObject(i) != null)
				result.add(uiValidations.getUserObject(i)); 
		}
		
		return result; 
	}
	
    protected void updateUi()
    {
        clear(true);
        
        if (uiValidations == null)
        {
            setSectionHTML(0, 0, "Loading validations"); // TODO 
        	return; 
        }
        

        if (showCurrentHierarchy(0))
            return;

        if (showDateEditor(2, null)) // TODO
            return;

        if (showEntityEditor(4))
            return;
        
        if (showValidationEditor(6))
        	return; 

        if (showListEntity(8, getEntityTypes()))
            return;
        
        ValueChangeEvent.fire(this, get());
    }
    
    private List<EmisMetaEntity> getEntityTypes()
    {
    	Set<EmisMetaEntity> entitySet = new HashSet<EmisMetaEntity>();
    	for (String validationId : currentValidationIds)
    	{
    		EmisValidation validation = NamedUtil.find(validationId, validations);
    		if (validation == null)
    			continue; 
    		
    		for (EmisValidationRule rule : validation.getRules())
    			entitySet.add(rule.getField(0).getEntity()); 
    	}

    	List<EmisMetaEntity> result = new ArrayList<EmisMetaEntity>(); 
    	for (EmisMetaEntity entity : getCurrentHierarchy().getEntityOrder())
    		if (entitySet.contains(entity))
    			result.add(entity); 

    	return result; 
    }
    
    private boolean setOnlyValidationId()
    {
    	int index = -1; 
    	for (int i = 0; i < uiValidations.getItemCount(); i++)
    	{
    		if (uiValidations.getUserObject(i) == null)
    			continue; 
    		
    		if (index != -1)
    			return false; 
    		
    		index = i; 
    	}
    	
    	if (index == -1)
    		return false; 
    	
    	uiValidations.setSelectedIndex(index);
    	currentValidationIds = getValidationIds(); 
    	return true; 
    }
    
    private boolean showValidationEditor(final int row)
    {
        setSectionHTML(row, 0, "Validations"); // TODO i18n
        if (this.currentValidationIds == null)
        {
        	if (!setOnlyValidationId())
        	{
        		editValidation(row);
        		return true;
        	}
        }

        show(row + 1, join(getValidationIds(), ", "), new ClickHandler() {
            public void onClick(ClickEvent event)
            { editValidation(row); }
        });

        return false;
    }
    
    private String join(List<String> items, String delim)
    {
    	StringBuffer result = new StringBuffer(); 
    	for (String item : items)
    	{
    		if (result.length() > 0)
    			result.append(delim); 
    		result.append(item); 
    	}
    	
    	return result.toString(); 
    }
    
    private void editValidation(int row)
    {
        setWidget(row + 1, 0, vp);
        removeFromRow(row + 2);
    }

	@Override
	protected PdfContentConfig getContentConfig(int paramInt) 
	{ return null; }

	@Override
	public void commit() 
	{
		super.commit();
		metaResult.setValidationIds(currentValidationIds);
		metaResult.setContext(getContext(true, true)); 
	}

	@Override
	public void set(ValidationMetaResult metaResult) 
	{
		super.set(metaResult);

		if (uiValidations == null)
			return; 

		for (int i = 0; i < uiValidations.getItemCount(); i++)
			uiValidations.setItemSelected(i, metaResult.hasValidationId(uiValidations.getUserObject(i).getId()));
	}
	
	private List<String> getValidationIds()
	{
		List<String> ids = new ArrayList<String>();
		for (int i = 0; i < uiValidations.getItemCount(); i++)
			if (uiValidations.isItemSelected(i))
			{
				if (uiValidations.getUserObject(i) != null)
					ids.add(uiValidations.getUserObject(i).getId()); 
			}
		
		return ids; 
	}
}
