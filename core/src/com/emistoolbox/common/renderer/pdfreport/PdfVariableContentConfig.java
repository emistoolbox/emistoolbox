package com.emistoolbox.common.renderer.pdfreport;

import java.util.List;
import java.util.Set;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;

public interface PdfVariableContentConfig extends PdfContentConfig
{
    public int getItemCount(); 
    
    public String getItemTitle(int i); 
    public String getItemVariable(int i); 

    public void addItem(String title, String variable);
    public void deleteItem(int i); 
    public void deleteAllItems(); 
    
    public List<String> getTitles(); 
    public List<String> getVariables(); 
    
    public Set<EmisMetaDateEnum> getUsedDateTypes();
}
