package com.emistoolbox.server.renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.emistoolbox.client.Message;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.server.model.EmisEntityData;
import com.emistoolbox.server.model.impl.EntityDataAccess;

public class HtmlFieldRenderer
{
	private EmisMetaData field; 
	private List<EmisMetaEnum> enumOrder = new ArrayList<EmisMetaEnum>(); 
	
	private EmisMetaEnum rowEnum = null; 
	private EmisMetaEnum colEnum = null; 
	private EmisMetaEnum subRowEnum = null; 
	private EmisMetaEnum subColEnum = null; 
	
	private EntityDataAccess access; 
	private EmisEntityData entityData; 
	
	public HtmlFieldRenderer(EmisMetaData field, EntityDataAccess access, EmisEntityData entityData)
	{
		this.field = field; 
		this.access = access; 
		this.entityData = entityData; 
		
		for (EmisMetaEnum e : field.getArrayDimensions().getEnums())
			enumOrder.add(e); 
		
		Collections.sort(enumOrder, new Comparator<EmisMetaEnum>() {
			@Override
			public int compare(EmisMetaEnum e1, EmisMetaEnum e2) 
			{ return -1 * Integer.compare(e1.getSize(), e2.getSize()); }
		});

		rowEnum = enumOrder.remove(0); 
		if (enumOrder.size() != 0)
			colEnum = enumOrder.remove(0); 
		
		if (enumOrder.size() != 0)
			subRowEnum = enumOrder.remove(0); 
		
		if (enumOrder.size() > 1)
			subColEnum = enumOrder.remove(1); 
	}
	
	public void renderHtml(StringBuffer result)
	{
		Map<EmisMetaEnum, String> enumValues = new HashMap<EmisMetaEnum, String>(); 
		if (rowEnum == null)
		{
			renderValues(result, enumValues);  
			return; 
		}

		result.append("<table class='emisResult'>"); 
		result.append("<thead>"); 
		renderColumnHeaders(result); 
		result.append("</thead>"); 
		
		// Iterate through row / column structure. 
		// 
		result.append("<tbody>"); 
		for (String rowValue : rowEnum.getValues())
		{
			enumValues.put(rowEnum, rowValue); 
			if (subRowEnum == null)
			{
				result.append("<tr>"); 
				result.append("<th>" + rowValue + "</th>"); 
				renderColumns(result, enumValues); 
				result.append("</tr>");
			}
			else
			{
				String[] subRowValues = subRowEnum.getValues(); 
				for (int i = 0; i < subRowValues.length; i++)
				{
					enumValues.put(subRowEnum, subRowValues[i]); 

					result.append("<tr>");
					if (i == 0)
					{
						result.append("<th valign='top' rowspan='" + subRowValues.length + "'>"); 
						result.append(rowValue); 
						result.append("</th>"); 
					}
					
					result.append("<th>"); 
					result.append(subRowValues[i]); 
					result.append("</th>"); 
					
					renderColumns(result, enumValues);
					
					result.append("</tr>");
				}
			}
		}
		
		result.append("</tbody>"); 
		result.append("</table>"); 
	}

	private void renderColumnHeaders(StringBuffer result)
	{
		result.append("<tr>"); 
		result.append("<th></th>"); 
		if (subRowEnum != null)
			result.append("<th></th>");
		
		if (colEnum == null)
			result.append("<th>Value</th>"); 
		else if (subColEnum == null)
		{
			for (String col : colEnum.getValues())
			{
				result.append("<th>"); 
				result.append(col); 
				result.append("</th>"); 
			}
		}
		else
		{
			for (String colValue : colEnum.getValues())
			{
				result.append("<th colspan='" + subColEnum.getValues().length + "'>"); 
				result.append(colValue); 
				result.append("</th>"); 
			}
		}
		
		result.append("</tr>"); 

		if (subColEnum != null)
		{
			result.append("<tr>"); 
			result.append("<th></th>"); 
			if (subRowEnum != null)
				result.append("<th></th>");

			for (String colValue : colEnum.getValues())
			{
				for (String subColValue : subColEnum.getValues())
				{
					result.append("<th>"); 
					result.append(subColValue); 
					result.append("</th>"); 
				}
			}
			result.append("</tr>"); 
		}
	}
	
	private void renderColumns(StringBuffer result, Map<EmisMetaEnum, String> enumValues)
	{
		if (colEnum == null)
		{
			result.append("<td>"); 
			renderValues(result, enumValues); 
			result.append("</td>"); 
		}
		else 
		{
			for (String colValue : colEnum.getValues())
			{
				enumValues.put(colEnum, colValue); 
				if (subColEnum == null)
				{
					result.append("<td>"); 
					renderValues(result, enumValues); 
					result.append("</td>"); 
				}
				else
				{
					String[] subColValues = subColEnum.getValues(); 
					for (int i = 0; i < subColValues.length; i++) 
					{
						enumValues.put(subColEnum, subColValues[i]); 

						result.append("<td>"); 
						renderValues(result, enumValues); 
						result.append("</td>"); 
					}
				}
			}
		}
	}
	
	private void renderValues(StringBuffer result, Map<EmisMetaEnum, String> enumValues)
	{
		if (enumOrder.size() == 0)
			result.append(getValue(enumValues));  
		else if (enumOrder.size() == 1)
		{
			for (String enumValue : enumOrder.get(0).getValues())
			{
				result.append(enumValue); 
				result.append("="); 
				enumValues.put(enumOrder.get(0), enumValue);
				result.append("<br />"); 
			}
		}
		else 
			result.append("Dimension count currently not supported"); 
	}
	
	private String getValue(Map<EmisMetaEnum, String> enumValues)
	{
		EmisMetaEnum[] enums = field.getArrayDimensions().getEnums(); 
		byte[] indexes = new byte[enums.length]; 
		for (int i = 0; i < enums.length; i++) 
			indexes[i] = enums[i].getIndex(enumValues.get(enums[i])); 

		return access.getAsString(entityData.getMasterArray(), field.getArrayDimensions().getIndex(indexes)); 		
	}
	
    public static String getHtml(EmisMetaData field, EntityDataAccess access, EmisEntityData entityData)
    {
        EmisMetaEnumTuple dimensions = field.getArrayDimensions();  
        EmisMetaEnum[] enums = dimensions.getEnums(); 
        byte[] sizes = new byte[enums.length]; 
        for (int i = 0; i < enums.length; i++) 
            sizes[i] = enums[i].getSize(); 

        switch (dimensions.getDimensions())
        {
        case 1: 
            return getOneDimensionHtml(enums[0], access, entityData.getMasterArray()); 

        case 2: 
            int rowIndex = getHighestIndex(sizes); 
            return getTwoDimensionHtml(dimensions, rowIndex, 1 - rowIndex, access, entityData.getMasterArray()); 

        case 3:
            rowIndex = getHighestIndex(sizes); 
            int subIndex = getSmallestIndex(sizes); 
            return getThreeDimensionHtml(dimensions, rowIndex, 3 - subIndex - rowIndex, subIndex, access, entityData.getMasterArray()); 

        case 4: 
        	return "not implemented"; 
        	
        case 5: 
        	return "not implemented"; 
        default: 
                throw new IllegalArgumentException("Unknown field dimension " + dimensions.getDimensions()); 
        }
    }
    
    private static String getOneDimensionHtml(EmisMetaEnum enumType, EntityDataAccess access, Object[] masterArray)
    {
        StringBuffer result = new StringBuffer(); 
        result.append("<table class='emisResult'>"); 
        result.append("<thead><tr>"); 
        for (String key : enumType.getValues()) 
        {
            result.append("<th>"); 
            result.append(key); 
            result.append("</th>\n"); 
        }
        result.append("</tr></thead>"); 
        result.append("<tbody><tr>\n");
        for (int i = 0; i < enumType.getSize(); i++) 
            result.append("<td>").append(access.getAsString(masterArray, i)).append("</td>\n"); 

        result.append("</tr></tbody>\n</table>\n"); 
        
        return result.toString(); 
    }
    
    private static String getTwoDimensionHtml(EmisMetaEnumTuple dimensions, int rowEnumIndex, int colEnumIndex, EntityDataAccess access, Object[] masterArray)
    {
        StringBuffer result = new StringBuffer(); 
        byte[] index = new byte[2]; 

        EmisMetaEnum rowEnum = dimensions.getEnums()[rowEnumIndex]; 
        EmisMetaEnum colEnum = dimensions.getEnums()[colEnumIndex]; 

        result.append("<table class='emisResult'>\n<thead><tr><th>&nbsp;</th>"); 
        for (byte col = 0; col < colEnum.getSize(); col++) 
            result.append("<th>").append(colEnum.getValue(col)).append("</th>\n"); 
        result.append("</tr></thead>"); 
        
        result.append("<tbody>"); 
        for (byte row = 0; row < rowEnum.getSize(); row++)
        {
            index[rowEnumIndex] = row; 
            
            result.append("<tr><th>");
            result.append(rowEnum.getValue(row)); 
            result.append("</th>\n"); 
            
            for (byte col = 0; col < colEnum.getSize(); col++)
            {
                index[colEnumIndex] = col; 
                result.append("  <td>"); 
                result.append(access.getAsString(masterArray, dimensions.getIndex(index))); 
                result.append("</td>\n");   
            }
            
            result.append("</tr>\n\n"); 
        }
        result.append("</tbody></table>\n"); 
        
        return result.toString(); 
    }
    
    private static String getThreeDimensionHtml(EmisMetaEnumTuple dimensions, int rowEnumIndex, int colEnumIndex, int subEnumIndex, EntityDataAccess access, Object[] masterArray)
    {
        StringBuffer result = new StringBuffer(); 
        byte[] index = new byte[3]; 

        EmisMetaEnum rowEnum = dimensions.getEnums()[rowEnumIndex]; 
        EmisMetaEnum colEnum = dimensions.getEnums()[colEnumIndex]; 
        EmisMetaEnum subEnum = dimensions.getEnums()[subEnumIndex]; 
        
        result.append("<table class='emisResult'>\n<thead><tr><td class='header'></td>"); 
        for (byte col = 0; col < colEnum.getSize(); col++) 
            result.append("<th>").append(colEnum.getValue(col)).append("<br><small>").append(StringUtils.join(subEnum.getValues(), "/")).append("</small></th>\n"); 
        result.append("</tr></thead>"); 

        result.append("<tbody>"); 
        for (byte row = 0; row < rowEnum.getSize(); row++) 
        {
            index[rowEnumIndex] = row; 
            
            result.append("<tr><th>"); 
            result.append(rowEnum.getValue(row)); 
            result.append("</th>"); 
            for (byte col = 0; col < colEnum.getSize(); col++) 
            {
                index[colEnumIndex] = col; 
                
                String[] values = new String[subEnum.getSize()];
                for (byte sub = 0; sub < subEnum.getSize(); sub++) 
                {
                    index[subEnumIndex] = sub; 
                    values[sub] = access.getAsString(masterArray, dimensions.getIndex(index)); 
                }
                
                result.append("  <td>"); 
                result.append(StringUtils.join(values, "/")); 
                result.append("</td>\n"); 
            }
            
            result.append("</tr>\n\n"); 
        }
        
        result.append("</tbody></table>"); 

        return result.toString(); 
    }
    
    private static byte getSmallestIndex(byte[] values)
    {
        byte result = 0; 
        for (byte i = 1; i < values.length; i++)
        {
            if (values[i] < values[result])
                result = i; 
        }
        
        return result; 
    }
    
    private static byte getHighestIndex(byte[] values)
    {
        byte result = 0; 
        for (byte i = 1; i < values.length; i++) 
        {
            if (values[i] >= values[result])
                result = i; 
        }
        
        return result; 
    }
}
