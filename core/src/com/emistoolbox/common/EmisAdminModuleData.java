package com.emistoolbox.common;

import com.emistoolbox.common.model.impl.EmisMetaImpl;
import com.emistoolbox.common.model.mapping.EmisDbMap;
import com.emistoolbox.common.model.mapping.impl.EmisDbMapImpl;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.validation.EmisValidation;

import java.io.Serializable;
import java.util.List;

public class EmisAdminModuleData implements Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisMeta model;
    private EmisDbMap mapping;
    private List<EmisValidation> validations; 
    
    public EmisAdminModuleData() 
    {
        this.model = new EmisMetaImpl();
        this.mapping = new EmisDbMapImpl();
        this.mapping.setMetaData(this.model);
    }

    public EmisMeta getModel()
    { return this.model; }

    public void setModel(EmisMeta model)
    {
        this.model = model;
        if (this.mapping != null)
            this.mapping.setMetaData(model);
    }

    public EmisDbMap getMapping()
    { return this.mapping; }

    public void setMapping(EmisDbMap mapping)
    { this.mapping = mapping; }
    
    public List<EmisValidation> getValidations() 
    { return validations; } 
    
    public void setValidations(List<EmisValidation> validations)
    { this.validations = validations; } 
}
