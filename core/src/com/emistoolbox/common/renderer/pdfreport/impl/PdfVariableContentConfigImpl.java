package com.emistoolbox.common.renderer.pdfreport.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.renderer.pdfreport.EmisTableStyle;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfigVisitor;
import com.emistoolbox.common.renderer.pdfreport.PdfVariableContentConfig;
import com.emistoolbox.common.util.NamedUtil;

public class PdfVariableContentConfigImpl extends PdfContentConfigImpl implements PdfVariableContentConfig
{
    private EmisMetaEntity entity; 
    private List<String> titles = new ArrayList<String>(); 
    private List<String> variables = new ArrayList<String>(); 
    
    private EmisTableStyle tableStyle; 

    public EmisMetaEntity getEntityType()
    { return entity; }

    public void setEntity(EmisMetaEntity entity)
    { this.entity = entity; }

    public EmisMetaEntity getSeniorEntity()
    { return entity; }

    public int getItemCount()
    { return variables.size(); } 

    public String getItemTitle(int i)
    { return titles.get(i); } 

    public String getItemVariable(int i)
    { return variables.get(i); } 

    public void addItem(String title, String variable)
    { 
        if (variable == null)
            throw new IllegalArgumentException("No variable name defined."); 
        
        titles.add(title == null ? "" : title); 
        variables.add(variable); 
    }

    public void deleteItem(int i)
    {
        variables.remove(i); 
        titles.remove(i); 
    }

    public void deleteAllItems()
    {
        variables.clear(); 
        titles.clear(); 
    }

    public List<String> getTitles()
    { return titles; }

    public List<String> getVariables()
    { return variables; }

	@Override
	public Set<EmisMetaDateEnum> getUsedDateTypes() 
	{
		Set<EmisMetaDateEnum> result = new HashSet<EmisMetaDateEnum>(); 
		for (int i = 0; i < variables.size(); i++) 
		{
			EmisMetaData field = NamedUtil.find(variables.get(i), entity.getData()); 
			
			EmisMetaDateEnum dateType = field.getDateType(); 
			while (dateType != null)
			{
				result.add(dateType); 
				dateType = dateType.getParent(); 
			}
		}
		
		return result; 
	}

	@Override
	public <T> T accept(PdfContentConfigVisitor<T> visitor) 
	{ return visitor.visit(this); }
	
	public EmisTableStyle getTableStyle()
	{ return tableStyle; } 
	
	public void setTableStyle(EmisTableStyle tableStyle) 
	{ this.tableStyle = tableStyle; }
}
