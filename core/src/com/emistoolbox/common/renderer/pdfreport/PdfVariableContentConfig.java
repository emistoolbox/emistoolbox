package com.emistoolbox.common.renderer.pdfreport;

import java.util.List;
import java.util.Set;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;

public interface PdfVariableContentConfig extends PdfContentConfig, TableStyleConfig
{
    public int getItemCount(); 
    
    public EmisMetaEntity getEntityType(); 
    
    public String getItemTitle(int i); 
    public String getItemVariable(int i); 

    public void addItem(String title, String variable);
    public void deleteItem(int i); 
    public void deleteAllItems(); 
    
    public List<String> getTitles(); 
    public List<String> getVariables(); 
    
    public Set<EmisMetaDateEnum> getUsedDateTypes();
}
