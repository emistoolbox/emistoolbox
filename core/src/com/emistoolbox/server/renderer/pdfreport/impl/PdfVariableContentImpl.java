package com.emistoolbox.server.renderer.pdfreport.impl;

import java.util.List;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityData;
import com.emistoolbox.server.model.EmisEntityDataSet;
import com.emistoolbox.server.model.impl.EntityDataAccess;
import com.emistoolbox.server.renderer.pdfreport.PdfContentVisitor;
import com.emistoolbox.server.renderer.pdfreport.PdfTableContent;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

public class PdfVariableContentImpl extends PdfTableContentBase implements PdfTableContent
{
    EmisMetaEntity entityType; 
    private List<String> names; 
    private List<String> variables; 

    private EmisEntityDataSet entityDataSet;  
    private int dateIndex; 
    private EmisEntity entity; 
    
    public PdfVariableContentImpl(String title, EmisMetaEntity entityType, List<String> names, List<String> variables)
    {
        setTitle(title); 

        this.names = names; 
        this.entityType = entityType; 
        this.variables = variables; 
    }
    
    public boolean setContext(EmisDataSet dataset, EmisContext context)
    { 
        List<EmisEntity> entities = context.getEntities();
        if (entities.size() != 1)
            return false; 

        EmisEntity entity = entities.get(0); 
        if (!NamedUtil.sameName(entity.getEntityType(), entityType)) 
            return false; 
        
        List<EmisEnumTupleValue> dates = context.getDates();
        if (dates.size() != 1)
            return false; 
        
        EmisEnumTupleValue date = dates.get(0);
        if (date.getEnumTuple().getDimensions() != 1)
            return false; 
        
        setContext(dataset, entity, context.getDateType(), date.getIndex()[0]); 
        return true; 
    }
    
    public void setContext(EmisDataSet dataset, EmisEntity entity, EmisMetaDateEnum dateType, int dateIndex)
    {
        this.entity = entity; 
        this.dateIndex = dateIndex;
        this.entityDataSet = dataset.getEntityDataSet(entity.getEntityType(), dateType); 
    }

    public PdfPTable getTable()
    {
        PdfPTable result = new PdfPTable(2); 
        result.setWidthPercentage(100.0f); 

        for (int i = 0; i < names.size(); i++)
        {
            PdfPCell cell = new PdfPCell(result.getDefaultCell());
            cell.setPhrase(new Phrase(names.get(i)));
            result.addCell(cell);
            
            cell = new PdfPCell(result.getDefaultCell()); 
            cell.setPhrase(new Phrase(getValue(variables.get(i)))); 
            result.addCell(cell); 
        }
        
        return result; 
    }
    
    private String getValue(String variable)
    {
        EntityDataAccess access = entityDataSet.getDataAccess(variable); 
        if (access == null)
            return "n/a";

        EmisEntityData data = entityDataSet.getData(dateIndex, entity.getId()); 
        
        EmisMetaData field = NamedUtil.find(variable, entity.getEntityType().getData()); 
        if (field.getType().equals(EmisDataType.STRING))
            return access.getAsString(data.getMasterArray(), 0); 

        int value = access.getAsInt(data.getMasterArray(), 0);
        if (value == -1)
            return ""; 
        if (field.getType().equals(EmisDataType.BOOLEAN))
            return value == 0 ? "no" : "yes"; // i18n
        if (field.getType().equals(EmisDataType.ENUM))
            return field.getEnumType().getValue((byte) value); 
        if (field.getType().equals(EmisDataType.ENUM_SET))
        	return field.getEnumType().getSetValues(value); 
        	
        return "" + value; 
    }
    
    public int getSize()
    { return names.size(); }

	@Override
	public <T> T accept(PdfContentVisitor<T> visitor) 
	{ return visitor.visit(this); }

	@Override
	public int getColumns() 
	{ return 2; }

	@Override
	public int getRows() 
	{ return names.size() + 1; }

	@Override
	public String getText(int row, int col) 
	{
		if (row == 0)
			return col == 0 ? "Name" : "Value"; 
		
		if (col == 0)
			return names.get(row - 1); 

		if (col == 1)
			getValue(variables.get(row - 1));  

		return null;
	} 
}
