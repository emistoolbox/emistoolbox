package com.emistoolbox.client.ui.validation;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.validation.EmisValidationRatioRule;

public class ValidationRuleRatioEditor extends ValidationRuleMinMaxEditor<EmisValidationRatioRule>
{
    public ValidationRuleRatioEditor(EmisMetaEntity entityType)
    { super(entityType, new String[] { "Numerator", "Denominator" }); }

    @Override
    public String getText()
    {
        EmisValidationRatioRule rule = get(); 
        return rule.getRuleName() + ": " + getFieldName(rule, 0) + "/" + getFieldName(rule, 1); 
    }
}
