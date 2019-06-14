package com.emistoolbox.client.ui.validation;

import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.validation.EmisValidationMinMaxRule;
import com.emistoolbox.common.model.validation.EmisValidationRule;
import com.emistoolbox.common.model.validation.impl.ValidationMinMaxRuleImpl;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.TextBox;

public class ValidationRuleMinMaxEditor<T extends EmisValidationMinMaxRule> extends ValidationRuleEditor<T>
{
    private TextBox uiMinValue = new TextBox();  
    private TextBox uiMaxValue = new TextBox(); 
    
    private static int[] acceptedChars = new int[] { 8, 9, 35, 36, 37, 38, 39, 40, 46, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57 }; 
    
    public ValidationRuleMinMaxEditor(EmisMetaEntity entityType)
    { 
        super(entityType, new String[] { "Field" }); 
        updateUi(); 
        
        KeyPressHandler doubleKeyHandler = new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) 
			{
				for (int code : acceptedChars)
				{
					if (event.getCharCode() == code)
						return; 							
				}

				((TextBox) event.getSource()).cancelKey(); 
			}
        }; 
        
        uiMinValue.addKeyPressHandler(doubleKeyHandler); 
        uiMaxValue.addKeyPressHandler(doubleKeyHandler); 
    } 

    public ValidationRuleMinMaxEditor(EmisMetaEntity entityType, String[] fields)
    { 
        super(entityType, fields); 
        updateUi(); 
    } 

    @Override
    protected void updateUi()
    {
    	updateNameUi(); 
    	
        setHTML(row, 0, "<b>Min value:</b>"); 
        setWidget(row, 1, uiMinValue); 
        row++; 
        
        setHTML(row, 0, "<b>Max value:</b>"); 
        setWidget(row, 1, uiMaxValue); 
        row++; 
        
        updateFieldsUi(); 
    }

    @Override
    public void commit()
    {
        super.commit();
        
        getItem().setMinValue(getValue(uiMinValue)); 
        getItem().setMaxValue(getValue(uiMaxValue)); 
    }

    @Override
    public void set(T rule)
    {
        super.set(rule);
        setValue(uiMinValue, rule.getMinValue()); 
        setValue(uiMaxValue, rule.getMaxValue());  
    }
    
    private void setValue(TextBox ui, Double value)
    { ui.setText(value == null ? "" : value.toString()); }

    private Double getValue(TextBox ui)
    {
        try { return Double.parseDouble(ui.getText()); }
        catch (NumberFormatException ex) 
        { return null; }
    }

    @Override
    public String getText()
    {
        T rule = get(); 
        return rule.getRuleName() + ": " + getValue("", rule.getMinValue(), " < ") + getFieldName(get(), 0) + getValue(" < ", rule.getMaxValue(), "");
    }
    
    private String getValue(String prefix, Double db, String postfix)
    {
        if (db == null)
            return ""; 
        
        return prefix + db + postfix; 
    }
}
