package com.emistoolbox.client.ui.validation;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.validation.EmisValidationNotExceedingRule;

public class ValidationRuleNotExceedingEditor extends ValidationRuleEditor<EmisValidationNotExceedingRule> 
{
    public ValidationRuleNotExceedingEditor(EmisMetaEntity entityType) 
    {
        super(entityType, new String[] { "Field", "Limit" }); 
        updateUi(); 
    } 

	@Override
	public String getText() 
	{
        EmisValidationNotExceedingRule rule = get(); 
        return getFieldName(rule, 0) + " < " + getFieldName(rule, 1); 
	}
}
