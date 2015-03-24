package com.emistoolbox.client.ui.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.GwtUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.model.validation.EmisValidationMinMaxRule;
import com.emistoolbox.common.model.validation.EmisValidationRule;
import com.emistoolbox.common.model.validation.impl.ValidationImpl;
import com.emistoolbox.common.model.validation.impl.ValidationMinMaxRuleImpl;
import com.emistoolbox.common.model.validation.impl.ValidationNotExceedingRule;
import com.emistoolbox.common.model.validation.impl.ValidationRatioRuleImpl;
import com.emistoolbox.common.model.validation.impl.ValidationTimeRatioRuleImpl;
import com.emistoolbox.common.util.Named;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class ValidationListEditor extends FlexTable implements EmisEditor<List<EmisValidation>>
{
    private Tree uiValidations = new Tree(); 
    private PushButton btnAddSet = new PushButton("Add Rule Set"); 
    private PushButton btnAddMinMaxRule = new PushButton("Add Min/MaxRule"); 
    private PushButton btnAddNotExceedRule = new PushButton("Add Not Exceed Rule"); 
    private PushButton btnAddTimeRatioRule = new PushButton("Add Time Ratio Rule"); 
    private PushButton btnAddRatioRule = new PushButton("Add Ratio Rule"); 
    private PushButton btnDelete = new PushButton("Delete"); 

    private ValidationEditor validationEditor; 
    
    private EmisEditor currentEditor; 
    private TreeItem currentItem; 
    
    public ValidationListEditor(EmisMeta meta)
    {
        setCellSpacing(5); 
        
        uiValidations.setStylePrimaryName("validationTree"); 
        
        validationEditor = new ValidationEditor(meta, this);
        validationEditor.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event)
            {
                if (currentItem != null)
                    currentItem.setText(event.getValue());
            }
        }); 
        
        FlexTable uiButtons = new FlexTable(); 
        uiButtons.setCellSpacing(2); 
        uiButtons.setWidget(0, 0, btnAddSet); 
        uiButtons.setWidget(1, 0, btnDelete); 
        uiButtons.setWidget(0, 1, btnAddMinMaxRule); 
        uiButtons.setWidget(1, 1, btnAddRatioRule); 
        uiButtons.setWidget(0, 2, btnAddNotExceedRule); 
//        uiButtons.setWidget(1, 2, btnAddTimeRatioRule); 
        
        EmisUtils.initSmall(btnAddSet, 100);
        EmisUtils.initSmall(btnAddMinMaxRule, 100);
        EmisUtils.initSmall(btnAddRatioRule, 100);
        EmisUtils.initSmall(btnAddNotExceedRule, 130);
        EmisUtils.initSmall(btnAddTimeRatioRule, 130);
        btnAddTimeRatioRule.setEnabled(false); 
        EmisUtils.initSmall(btnDelete, 100);

        HTML title = new HTML("Rule Sets"); 
        title.setStylePrimaryName("title");
        title.addStyleName("titleLine");
        setWidget(1, 0, title); 
        
        setWidget(0, 0, uiButtons); 
        getFlexCellFormatter().setColSpan(0,  0,  2);  
        
        getFlexCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP); 
        setWidget(2, 0, uiValidations); 
        getCellFormatter().setHeight(2, 0, "100%"); 

        updateButtons(false); 
        
        uiValidations.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> event)
            {
                Object item = event.getSelectedItem().getUserObject();
                boolean showAddButtons = true; 
                if (item instanceof EmisValidation)
                    show(event.getSelectedItem(), (EmisValidation) item); 
                else if (item instanceof EmisValidationRule)
                    show(event.getSelectedItem(), (EmisValidationRule) item); 
                else
                {
                	showAddButtons = false; 
                    show(); 
                }
                
                updateButtons(showAddButtons); 
            }
        }); 
        
        btnAddSet.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event)
            {
                TreeItem item = new TreeItem("(new)"); 
                item.setUserObject(new ValidationImpl()); 
                uiValidations.addItem(item); 
                uiValidations.setSelectedItem(item); 
            }
        }); 
        
        btnAddMinMaxRule.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event)
            {
                commit(); 
                addNewRule(new ValidationMinMaxRuleImpl()); 
            }
        }); 
        
        btnDelete.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event)
            {
                TreeItem item = uiValidations.getSelectedItem(); 
                if (item == null)
                	return; 
                
                TreeItem parent = item.getParentItem(); 
                item.remove(); 
               
        		show(); 
                if (parent != null)
                	uiValidations.setSelectedItem(parent);
                else if (uiValidations.getItemCount() > 0)
                	uiValidations.setSelectedItem(uiValidations.getItem(0)); 
            }
        });
        
        btnAddNotExceedRule.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{
				commit(); 
				addNewRule(new ValidationNotExceedingRule()); 
			}
        }); 
        
        btnAddRatioRule.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event)
            {
                commit(); 
                addNewRule(new ValidationRatioRuleImpl()); 
            }
            
        }); 
        
        for (int i = 0; i < getRowCount(); i++)
            getRowFormatter().setVerticalAlign(i,  HasVerticalAlignment.ALIGN_TOP);
        
        getColumnFormatter().setWidth(1, "450px"); 
    }

    void updateButtons(boolean showAddButtons)
    {
    	btnAddMinMaxRule.setEnabled(showAddButtons);
    	btnAddNotExceedRule.setEnabled(showAddButtons); 
    	btnAddRatioRule.setEnabled(showAddButtons); 
    }
    
    void removeChildItems()
    { 
    	if (currentItem != null)
    		currentItem.removeItems(); 
    }
    private void addNewRule(EmisValidationRule rule)
    {
        TreeItem validationItem = getCurrentValidationItem(); 
        if (validationItem == null)
            return; 
       
        TreeItem item = new TreeItem(rule.getRuleName()); 
        item.setUserObject(rule); 
        validationItem.addItem(item); 
        uiValidations.setSelectedItem(item); 
        uiValidations.ensureSelectedItemVisible();
    }
    
    private TreeItem getCurrentValidationItem()
    {
        TreeItem item = uiValidations.getSelectedItem();
        while (item != null && !(item.getUserObject() instanceof EmisValidation))
            item = item.getParentItem(); 
        
        return item; 
    }
    
    @Override
    public void commit()
    {
        if (currentEditor != null)
            currentEditor.commit();
    }

    @Override
    public List<EmisValidation> get()
    {
        commit(); 
        
        List<EmisValidation> result = new ArrayList<EmisValidation>();
        
        for (int i = 0; i < uiValidations.getItemCount(); i++) 
        {
            TreeItem validationItem = uiValidations.getItem(i); 
            EmisValidation validation = (EmisValidation) validationItem.getUserObject();

            List<EmisValidationRule> rules = new ArrayList<EmisValidationRule>(); 
            for (int r = 0; r < validationItem.getChildCount(); r++)
                rules.add((EmisValidationRule) validationItem.getChild(r).getUserObject()); 

            validation.setRules(rules); 
            result.add(validation); 
        }
        
        return result;
    }

    @Override
    public void set(List<EmisValidation> validations)
    {
        uiValidations.removeItems(); 
        show(); 

        if (validations == null)
            return; 
        
        for (EmisValidation validation : validations)
        {
            TreeItem item = uiValidations.addItem(validation.getName());
            item.setUserObject(validation); 
            
            for (EmisValidationRule rule : validation.getRules())
            {
            	String name = rule.getName(); 
            	if (name == null || rule.getName().equals(""))
            		name = rule.getRuleName(); 
            	else
            		name = rule.getRuleName() + ":" + name;
            	
                TreeItem ruleItem = item.addItem(name);
                ruleItem.setUserObject(rule);
            }
        }
        
        if (uiValidations.getItemCount() > 0)
        	uiValidations.setSelectedItem(uiValidations.getItem(0)); 
    }

    private void show()
    { 
        // Show empty. 
    	HTML title = new HTML("Rules"); 
        title.setStylePrimaryName("title");
        title.addStyleName("titleLine");
        setWidget(1, 1, title); 
        setHTML(2, 1, ""); 
    }
    
    private void show(TreeItem item, EmisValidation validation)
    {
        if (currentEditor != null)
        {
            currentEditor.commit(); 
            updateName(currentItem);  
        }

        validationEditor.set(validation); 
        currentEditor = validationEditor; 
        currentItem = item; 
        
        show("Rule Set", (Widget) currentEditor); 
    }
    
    private void updateName(TreeItem item)
    {
        Object obj = item.getUserObject();
        if (obj instanceof ValidationRuleEditor)
        {
        	EmisValidationRule rule = ((ValidationRuleEditor) obj).get(); 
        	item.setText(rule.getRuleName() + ": " + rule.getName());
        }
    }
    
    private void show(TreeItem ruleItem, EmisValidationRule rule)
    {
        TreeItem validationItem = ruleItem; 
        while (validationItem != null && !(validationItem.getUserObject() instanceof EmisValidation))
            validationItem = validationItem.getParentItem(); 
        
        if (validationItem == null)
        {
            show(); 
            return; 
        }
        
        show(ruleItem, rule, (EmisValidation) validationItem.getUserObject()); 
    }
    
    private void show(TreeItem ruleItem, EmisValidationRule rule, EmisValidation validation)
    {
        ValidationRuleEditor editor = null; 
        
        // show rule - depends on which rule. 
        if (rule instanceof ValidationRatioRuleImpl)
            editor = new ValidationRuleRatioEditor(validation.getEntityType()); 
        else if (rule instanceof ValidationMinMaxRuleImpl)
            editor = new ValidationRuleMinMaxEditor<ValidationMinMaxRuleImpl>(validation.getEntityType());
        else if (rule instanceof ValidationNotExceedingRule)
        	editor = new ValidationRuleNotExceedingEditor(validation.getEntityType()); 
        else
            editor = null; 

        currentEditor = editor; 
        if (editor != null)
        {
            final ValidationRuleEditor ruleEditor = editor; 
            
            editor.set(rule); 
            editor.addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event)
                {
                    if (currentItem != null)
                        currentItem.setText(event.getValue()); 
                }
            }); 
            
            editor.focusField(null);
        }
        
        currentItem = currentEditor == null ? null : ruleItem; 
        
        show("Rule " + rule.getRuleName() + " Validation", editor);
    }
    
    private void show(String title, Widget editor)
    {
        HTML htmlTitle = new HTML(title); 
        htmlTitle.setStylePrimaryName("title");
        htmlTitle.addStyleName("titleLine");
        setWidget(1, 1, htmlTitle); 
        
        setWidget(2, 1, editor); 
    }

    
    public boolean validate()
    {
    	for (int i = 0; i < uiValidations.getItemCount(); i++) 
    	{
    		Map<String, String> errors = new HashMap<String, String>(); 
    		if (!validate(uiValidations.getItem(i), errors))
    			return false; 
    	}
    	
    	return true; 
    }
    
    public boolean validate(TreeItem item, Map<String, String> errors)
    {
    	Object userObject = item.getUserObject(); 
    	boolean result = true; 
    	if (userObject instanceof EmisValidation)
    		result = validate((EmisValidation) userObject, errors); 
    	else if (userObject instanceof EmisValidationRule)
    		result = validate((EmisValidationRule) userObject, errors); 

    	if (!result)
    	{
    		showError(item, errors); 
    		return false; 
    	}
    	
    	for (int i = 0; i < item.getChildCount(); i++)
    	{
    		TreeItem child = item.getChild(i); 
    		if (!validate(child, errors))
    			return false; 
    	}
    	
    	return true; 
    }
    
    private void showError(TreeItem item, Map<String, String> errors)
    {
		StringBuffer keys = new StringBuffer(); 
		for (String key : errors.keySet())
		{
			if (keys.length() > 0)
				keys.append(", "); 
			keys.append(key); 
		}
		
		Window.alert("Please enter missing fields: " + keys);
		uiValidations.setSelectedItem(item);
    }
    
    public boolean validate(EmisValidation val, Map<String, String> errors)
    {
    	if (val.getId() == null || val.getId().equals(""))
    	{
    		errors.put("id", "error.validate.missingField"); 
    		return false; 
    	}
    	
    	return true; 
    }
    
    public boolean validate(EmisValidationRule rule, Map<String, String> errors)
    {
    	int size = errors.size(); 

    	if (rule.getName() == null || rule.getName().equals(""))
    		errors.put("name", "error.validate.missingField"); 
    	
    	for (int i = 0; i < rule.getFieldCount(); i++) 
    	{
    		if (null == rule.getField(i))
    			errors.put(rule.getFieldName(i), "error.validate.missingField");
    	}

    	if (rule instanceof ValidationMinMaxRuleImpl)
    	{
    		EmisValidationMinMaxRule minMaxRule = (EmisValidationMinMaxRule) rule; 
    		if (minMaxRule.getMinValue() == null && minMaxRule.getMaxValue() == null)
    			errors.put("min or max", "error.validate.missingField"); 
    	}
    	
    	return errors.size() == size; 
    }
}

class ValidationEditor extends FlexTable implements EmisEditor<EmisValidation>, HasValueChangeHandlers<String>
{
    private EmisValidation validation; 
    
    private ListBoxWithUserObjects<EmisMetaEntity> uiEntities = new ListBoxWithUserObjects<EmisMetaEntity>(); 
    private TextBox uiId = new TextBox(); 
    private AdditionalFieldsEditor uiFields; 
    private EmisMeta meta; 
    
    public ValidationEditor(EmisMeta meta, final ValidationListEditor listEditor)
    {
    	this.meta = meta; 

    	uiFields = new AdditionalFieldsEditor();
    	
        for (EmisMetaEntity entity : meta.getEntities())
        {
        	if (hasNumericFields(entity))
        		uiEntities.addItem(entity.getName(), entity);
        }
        
        uiEntities.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) 
			{ listEditor.removeChildItems(); }
        });
        
        setHTML(0, 0, "<b>ID:</b>"); 
        setWidget(0, 1, uiId); 
        
        setHTML(1, 0, "<b>Location:</b>"); 
        setWidget(1, 1, uiEntities);
        setHTML(2, 1, "<small>Careful - changing this value will delete all contained rules.</small>"); 
        getRowFormatter().setVerticalAlign(2, HasVerticalAlignment.ALIGN_TOP);
        
        setHTML(3, 0, "<b>Display:</b>"); 
        setWidget(3, 1, uiFields); 
        getRowFormatter().setVerticalAlign(3, HasVerticalAlignment.ALIGN_TOP);
        
        uiId.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event)
            { updateName(); }
        }); 
        
        uiEntities.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				uiFields.updateEntities(getEntities(uiEntities.getUserObject())); 
			}
        });

		uiFields.updateEntities(getEntities(uiEntities.getUserObject())); 
    }
    
    private boolean hasNumericFields(EmisMetaEntity entity)
    {
    	for (EmisMetaData field : entity.getData())
    	{
    		if (field.getType() == EmisDataType.BYTE || field.getType() == EmisDataType.BOOLEAN || field.getType() == EmisDataType.INTEGER || field.getType() == EmisDataType.BYTE)
    			return true; 
    	}
    	
    	return false; 
    }
    
    private void updateName()
    {
    	String name = uiId.getText(); 
    	if (name == null || name.equals(""))
    		name = "(none)";
    	
    	ValueChangeEvent.fire(this, name);
    }
    
    private List<EmisMetaEntity> getEntities(EmisMetaEntity entity)
    {
    	Set<String> entityNames = new HashSet<String>(); 
    	
    	for (EmisMetaHierarchy hierarchy : meta.getHierarchies())
    	{
    		NamedIndexList<EmisMetaEntity> order = hierarchy.getEntityOrder(); 
    		int index = NamedUtil.findIndex(entity, order);
    		if (index == -1)
    			continue; 
    		
    		for (int i = 0; i <= index; i++)
    			entityNames.add(order.get(i).getName()); 
    	}
    	
    	List<EmisMetaEntity> result = new ArrayList<EmisMetaEntity>(); 
    	for (String name : entityNames)
    	{
    		int i = meta.getEntities().getIndex(name);
    		if (i != -1)
    			result.add(meta.getEntities().get(i)); 
    	}

    	return result;
    }
    
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler)
    { return addHandler(handler, ValueChangeEvent.getType()); }

    @Override
    public void commit()
    {
        validation.setId(uiId.getText()); 
        validation.setEntityType(uiEntities.getUserObject());
        validation.clearAdditionalFields(); 
        for (EmisMetaData field : uiFields.get())
        	validation.addAdditionalField(field); 
    }

    @Override
    public EmisValidation get()
    {
        commit(); 
        return validation; 
    }

    @Override
    public void set(EmisValidation validation)
    {
        this.validation = validation; 
        GwtUtils.setTextBox(uiId, validation.getId());
        uiFields.set(validation.getAdditionalFields()); 
        
        if (validation.getEntityType() != null)
        {
            for (int i = 0; i < uiEntities.getItemCount(); i++)
            {
                if (NamedUtil.sameName(uiEntities.getUserObject(i), validation.getEntityType()))
                    uiEntities.setSelectedIndex(i);
            }
        }

        uiFields.updateEntities(getEntities(validation.getEntityType()));
    }
    
    public void focusField(String name)
    { uiId.setFocus(true); }
}
