package com.emistoolbox.common.results;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.impl.EmisEnumUtils;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaGroupEnum;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.results.impl.MetaResultDimensionDate;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntity;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityAncestors;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityChildren;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityFilter;
import com.emistoolbox.common.results.impl.MetaResultDimensionEnum;
import com.emistoolbox.common.results.impl.MetaResultUtil;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.NamedUtil;

public class MetaResultDimensionUtil
{
	public static final int MAX_VERTICAL_DIMENSIONS = 3; 
	
    public static List<MetaResultDimension> getHorizontalDimensions(MessageAdmin msgAdmin, EmisMetaEntity lowestEntity, EmisMetaHierarchy hierarchy)
    {
        NamedIndexList<EmisMetaEntity> entities = hierarchy.getEntityOrder(); 
        
        int index = NamedUtil.findIndex(lowestEntity, entities);
        if (index == -1)
            return null;

        List<MetaResultDimension> result = new ArrayList<MetaResultDimension>(); 
        result.add(getChildrenDimension(msgAdmin.metadeInfoAllEnums(((EmisMetaEntity) entities.get(0)).getName()), null, hierarchy));

        for (int i = 0; i < index; i++)
        {
            EmisMetaEntity parent = (EmisMetaEntity) entities.get(i);
            EmisMetaEntity child = (EmisMetaEntity) entities.get(i + 1);
            result.add(getChildrenDimension(msgAdmin.metadeInfoAllChildrenForParent(child.getName(), parent.getName()), parent, hierarchy));
        }

        return result; 
    }
    
    public static List<MetaResultDimension> getVerticalDimensions(MessageAdmin msgAdmin, EmisMetaEntity lowestEntity, EmisMetaHierarchy hierarchy)
    {
        NamedIndexList<EmisMetaEntity> entities = hierarchy.getEntityOrder(); 
        int index = NamedUtil.findIndex(lowestEntity, entities);
        if (index == -1)
            return null;

        List<MetaResultDimension> result = new ArrayList<MetaResultDimension>(); 

        for (int i = 0; i < index; i++)
        {
            EmisMetaEntity child = (EmisMetaEntity) entities.get(i + 1);
            result.add(getAncestorDimension(msgAdmin.metadeInfoOneChildAndItsParents(child.getName(), getParentList(entities, i + 1)), child, hierarchy));
        }
        
        return result; 
    }
    
    private static String getParentList(NamedIndexList<EmisMetaEntity> entities, int toIndex)
    {
    	StringBuffer result = new StringBuffer(); 

    	int startIndex = Math.max(0,  toIndex - MAX_VERTICAL_DIMENSIONS + 1); 
    	for (int i = startIndex; i < toIndex; i++)
    	{
    		if (i > startIndex)
    			result.append(", "); 
    		result.append(entities.get(i).getName()); 
    	}
    	
    	return result.toString(); 
    }
    
    public static List<MetaResultDimension> getEnumDimensions(MessageAdmin msgAdmin, EmisIndicator indicator, EmisMetaEnum ignoreEnumType, EmisReportConfig reportConfig)
    {
        List<MetaResultDimension> result = new ArrayList<MetaResultDimension>(); 

        List<EmisMetaEnum> enums = NamedUtil.sort(indicator.getSplitEnums());
        for (EmisMetaEnum enumType : enums)
        {
            if (NamedUtil.sameName(enumType, ignoreEnumType))
                continue;

            MetaResultDimensionEnum metaDimension = new MetaResultDimensionEnum();
            metaDimension.setEnumType(enumType);
            metaDimension.setName(msgAdmin.metadeInfoByEnumName(enumType.getName()));
            result.add(metaDimension);
            
            if (reportConfig == null)
            	continue; 
            
            for (EmisMetaGroupEnum groupEnumType : reportConfig.getGroupEnums(enumType))
            {
                MetaResultDimensionEnum groupMetaDimension = new MetaResultDimensionEnum();
                groupMetaDimension.setEnumType(groupEnumType);
                groupMetaDimension.setName(msgAdmin.metadeInfoByEnumName(enumType.getName()));
                result.add(groupMetaDimension);
            }
        }
        
        return result; 
    }
    
    public static List<MetaResultDimension> getEntityFilterDimensions(MessageAdmin msgAdmin, EmisMetaEntity seniorEntity, MetaResultDimension ignoreDimension)
    {
        List<MetaResultDimension> result = new ArrayList<MetaResultDimension>(); 

        EmisMetaData ignoreField = null;
        if ((ignoreDimension instanceof MetaResultDimensionEntityFilter))
        {
            ignoreField = ((MetaResultDimensionEntityFilter) ignoreDimension).getField();
            if (!NamedUtil.sameName(ignoreField.getEntity(), seniorEntity))
                ignoreField = null;
        }

        for (EmisMetaData field : seniorEntity.getData())
        {
            if (NamedUtil.sameName(ignoreField, field))
                continue;

            if ((field.getType() == EmisMetaData.EmisDataType.BOOLEAN) || field.getType() == EmisMetaData.EmisDataType.ENUM || field.getType() == EmisMetaData.EmisDataType.ENUM_SET)
                result.add(getEntityFilterDimension(msgAdmin, field));
        }
        
        return result; 
    }
    
    public static List<MetaResultDimension> getDateDimensions(MessageAdmin msgAdmin, EmisIndicator indicator, EmisMetaDateEnum ignoreDateType, EmisReportConfig reportConfig)
    {
        List<MetaResultDimension> result = new ArrayList<MetaResultDimension>(); 

        Set<EmisMetaDateEnum> dateTypes = new HashSet<EmisMetaDateEnum>();
        MetaResultUtil.addUsedDateTypes(dateTypes, indicator.getUsedDateTypes(), true);
        for (EmisMetaDateEnum dateType : EmisEnumUtils.sort(dateTypes))
        {
        	if (ignoreDateType != null && NamedUtil.sameName(ignoreDateType, dateType))
        		continue; 
        	
            MetaResultDimensionDate metaDimension = new MetaResultDimensionDate();
            metaDimension.setDateEnumType(dateType);
        	if (dateType.getParent() == null) 
        		metaDimension.setName(msgAdmin.metadeInfoAllEnums(dateType.getName())); 
        	else
        		metaDimension.setName(msgAdmin.metadeInfoAllEnumsFor(dateType.getName(), dateType.getParent().getName())); 
        	
        	result.add(metaDimension); 
        	
        	if (reportConfig == null)
        		continue; 

        	/*
        	for (EmisMetaGroupEnum groupEnum : reportConfig.getGroupEnums(dateType))
        	{
        		MetaResultDimensionDate groupMetaDimension = new MetaResultDimensionDate(); 
        		groupMetaDimension.setDateEnumType(groupEnum);
        		if (((EmisMetaDateEnum) groupEnum.getBaseEnum()).getParent() == null)
            		groupMetaDimension.setName(msgAdmin.metadeInfoAllEnums(groupEnum.getName())); 
        		else
        			groupMetaDimension.setName(msgAdmin.metadeInfoAllEnumsFor(groupEnum.getName(), ((EmisMetaDateEnum) groupEnum.getBaseEnum()).getParent().getName())); 
        	}
        	*/ 
        }

        return result;
    }

    public static void addDimensions(MessageAdmin msgAdmin, MetaResultDimensionBuilder result, EmisMetaHierarchy hierarchy, EmisIndicator indicator, MetaResultDimension ignoreDimension, EmisMeta meta)
    {
        EmisMetaEntity lowestEntity = indicator.getSeniorEntity(hierarchy); 
        if ((ignoreDimension == null) || (!(ignoreDimension instanceof MetaResultDimensionEntity)))
        {
            addDimensions(result, msgAdmin.metadeInfoHorizontalAnalysis(), MetaResultDimensionUtil.getHorizontalDimensions(msgAdmin, lowestEntity, hierarchy)); 
            addDimensions(result, msgAdmin.metadeInfoVerticalAnalysis(), MetaResultDimensionUtil.getVerticalDimensions(msgAdmin, lowestEntity, hierarchy)); 
        }

        EmisMetaDateEnum lowestDate = indicator.getSeniorDateEnum();
        if (lowestDate == null)
            lowestDate = meta.getDefaultDateType();

        // Date dimensions. 
        EmisMetaDateEnum ignoreDateType = null; 
        if (ignoreDimension instanceof MetaResultDimensionDate)
        	ignoreDateType = ((MetaResultDimensionDate) ignoreDimension).getDateEnumType();
        addDimensions(result, msgAdmin.metadeInfoDateAnalysis(), MetaResultDimensionUtil.getDateDimensions(msgAdmin, indicator, ignoreDateType, null)); // TODO EmisReportConfig 

        if ((ignoreDimension instanceof MetaResultDimensionEnum))
            addDimensions(result, msgAdmin.metadeInfoEnumAnalysis(), MetaResultDimensionUtil.getEnumDimensions(msgAdmin, indicator, ((MetaResultDimensionEnum) ignoreDimension).getEnumType(), null)); // TODO EmisReportConfig 
        else
            addDimensions(result, msgAdmin.metadeInfoEnumAnalysis(), MetaResultDimensionUtil.getEnumDimensions(msgAdmin, indicator, null, null)); // TODO EmisReportConfig 

        addDimensions(result, msgAdmin.metadeInfoByEntityVariables(), MetaResultDimensionUtil.getEntityFilterDimensions(msgAdmin, indicator.getSeniorEntity(hierarchy), ignoreDimension));
    }
    
    private static void addDimensions(MetaResultDimensionBuilder result, String title, List<MetaResultDimension> dims)
    {
        if (dims == null || dims.size() == 0)
            return; 
        
        if (title != null)
            result.add(title); 
        
        for (MetaResultDimension dim : dims)
            result.add(dim); 
    }

    private static MetaResultDimension getAncestorDimension(String name, EmisMetaEntity child, EmisMetaHierarchy hierarchy)
    {
        MetaResultDimensionEntity metaDim = new MetaResultDimensionEntityAncestors();
        metaDim.setName(name);
        metaDim.setHierarchy(hierarchy);
        metaDim.setEntityType(child);
        return metaDim;
    }

    private static MetaResultDimension getChildrenDimension(String name, EmisMetaEntity entity, EmisMetaHierarchy hierarchy)
    {
        MetaResultDimensionEntity metaDim = new MetaResultDimensionEntityChildren();
        metaDim.setName(name);
        metaDim.setHierarchy(hierarchy);
        metaDim.setEntityType(entity);
        return metaDim;
    }

    private static MetaResultDimension getEntityFilterDimension(MessageAdmin msgAdmin, EmisMetaData field)
    {
        MetaResultDimensionEntityFilter result = new MetaResultDimensionEntityFilter();
        result.setName(msgAdmin.metadeInfoEntityByName(field.getEntity().getName(), field.getName()));
        result.setField(field);

        return result;
    }

    public static enum ENTITY_DATE_LEVEL 
    { NONE, GENERIC, NAMES; }

    
    public static String getTitle(MetaResult metaResult, ENTITY_DATE_LEVEL entityAndDateLevel, boolean asHtml)
    { return getTitle(metaResult, entityAndDateLevel, asHtml, false); } 
    
    public static String getTitle(MetaResult metaResult, ENTITY_DATE_LEVEL entityAndDateLevel, boolean asHtml, boolean showEntityPathNames)
    {
        StringBuffer result = new StringBuffer();
        if (metaResult.getIndicator() != null)
        {
            if (asHtml)
                result.append("<div class='title'>");
            result.append(metaResult.getIndicator().getName());
            if (asHtml)
                result.append("</div><div class='subtitle'>");
            else
                result.append(" - ");
        }
        boolean anyEntity = false;

        Set<EmisMetaDateEnum> dateTypes = metaResult.getUsedDateTypes();

        String seperator = "";
        if ((metaResult instanceof TableMetaResult))
        {
            TableMetaResult tableMetaResult = (TableMetaResult) metaResult;
            
            for (int i = 0; i < tableMetaResult.getDimensionCount(); i++)
            {
                MetaResultDimension dimension = tableMetaResult.getDimension(i);

                // Remove date types that are shown on axises. 
                MetaResultUtil.removeDateType(dimension, dateTypes);

                String text = dimension.getName();
                if ((dimension instanceof MetaResultDimensionEntity))
                {
                    anyEntity = true;
                    if (entityAndDateLevel == ENTITY_DATE_LEVEL.GENERIC)
                    {
                        EmisMetaEntity entityType = ((MetaResultDimensionEntity) dimension).getEntityType();
                        if (entityType != null)
                            text = entityType.getName();
                    }
                    else if (entityAndDateLevel == ENTITY_DATE_LEVEL.NONE)
                        text = "";
                }
                if ((dimension instanceof MetaResultDimensionDate))
                {
                    if (entityAndDateLevel == ENTITY_DATE_LEVEL.GENERIC)
                        text = ((MetaResultDimensionDate) dimension).getDateEnumType().getName();
                    else if (entityAndDateLevel == ENTITY_DATE_LEVEL.NONE)
                        text = "";
                }
                result.append(seperator); 
                seperator = "; ";

                result.append(text);
            }
        }
        
        if ((!anyEntity) && (entityAndDateLevel != ENTITY_DATE_LEVEL.NONE))
        {
        	String entitySeperator = seperator; 
            for (EmisEntity entity : metaResult.getContext().getEntities())
            {
                if (entity.getEntityType() == null)
                    continue;

                result.append(entitySeperator); 
                entitySeperator = ", "; 
                seperator = "; "; 

                result.append(entity.getEntityType().getName());
                result.append(" ");
                result.append(entity.getName());
            }

            if (showEntityPathNames && metaResult instanceof ReportMetaResult)
            {
                String[] names = ((ReportMetaResult) metaResult).getEntityPathNames();
                if (names.length > 2)
                {
                    result.append(" (");
                    String delim = ""; 
                    for (int i = Math.min(names.length - 2, 2); i > 0; i--) 
                    {
                        result.append(delim);
                        delim = ", ";
                        result.append(names[i]);
                    }

                    result.append(")");
                }
            }
        }
        
        if (dateTypes.size() > 0 && entityAndDateLevel != ENTITY_DATE_LEVEL.NONE)
        {
            EmisMetaDateEnum targetDateType = EmisEnumUtils.findLowestEnum(dateTypes); 
            seperator += targetDateType.getName() + ": "; 

            if (metaResult.getContext().getDates() != null)
            {
                for (EmisEnumTupleValue value : metaResult.getContext().getDates())
                {
                	result.append(seperator);
                	seperator = ", ";

                	if (entityAndDateLevel == ENTITY_DATE_LEVEL.GENERIC)
                	{
                		EmisMetaEnum[] enums = value.getEnumTuple().getEnums();
                		result.append(enums[(value.getEnumTuple().getDimensions() - 1)].getName());
                		break;
                	}

                    result.append(EmisEnumUtils.getValueAs(value, dateTypes).toString()); 
                }
            }
        }

        if (asHtml)
        	result.append("</div>"); 

        return result.toString();
    }

    public static String getTitle(MetaResult metaResult)
    { return getTitle(metaResult, ENTITY_DATE_LEVEL.NAMES, true); }
}
