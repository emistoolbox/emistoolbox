package com.emistoolbox.common.model.mapping;

import java.util.Iterator;
import java.util.Map;

import com.emistoolbox.common.model.mapping.impl.DbRowByColumnAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowByColumnIndexAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowEnumAccess;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.NamedUtil;

/** Class used to validate the mapping configuration after a possible change to the model. */
public class MappingValidation
{
    private EmisMeta model;
    
    public MappingValidation(EmisMeta model)
    { this.model = model; }

    public void validateMappings(EmisDbMap mapping) 
    {
        Iterator<EmisHierarchyDbMap> hierarchyIter = mapping.getHierarchyMappings().iterator(); 
        while (hierarchyIter.hasNext())
        {
            if (!validateMapping(hierarchyIter.next()))
                hierarchyIter.remove(); 
        }
        
        Iterator<EmisEntityDbMap> entityIter = mapping.getEntityMappings().iterator(); 
        while (entityIter.hasNext())
        {
            if (!validateMapping(entityIter.next()))
                entityIter.remove(); 
        }
    }
    
    private boolean validateMapping(EmisHierarchyDbMap map)
    {
        // Ensure the hierarchy still exists. 
        EmisMetaHierarchy hierarchy = NamedUtil.find(map.getHierarchy().getName(), model.getHierarchies());   
        if (hierarchy == null)
            return false; 
        
        // Validate each mapping. 
        Iterator<EmisHierarchyDbMapEntry> iter = map.getMappings().iterator(); 
        while (iter.hasNext())
        {
            if (!validateMapping(iter.next(), hierarchy))
                iter.remove(); 
        }
        
        return true; 
    }
    
    private boolean validateMapping(EmisHierarchyDbMapEntry map, EmisMetaHierarchy hierarchy)
    {
        int childPos = NamedUtil.findIndex(map.getChildEntity(), hierarchy.getEntityOrder()); 
        int parentPos = NamedUtil.findIndex(map.getParentEntity(), hierarchy.getEntityOrder());  

        return childPos != -1 && parentPos != -1 && parentPos + 1 == childPos; 
    }
    
    private boolean validateMapping(EmisEntityDbMap map)
    {
        // Ensure entity still exists. 
        EmisMetaEntity entity = NamedUtil.find(map.getEntity().getName(), model.getEntities());
        if (entity == null)
            return false;
        
        // Ensure all fields still exist. 
        Map<String, DbRowAccess> accesses = map.getFieldAccess(); 
        for (String fieldName : accesses.keySet())
        {
            EmisMetaData field = NamedUtil.find(fieldName, entity.getData()); 
            if (field == null)
                accesses.remove(fieldName);
            else if (!validateMapping(field, accesses.get(fieldName)))
                accesses.remove(fieldName); 
        }

        return accesses.size() > 0; 
    }
    
    private boolean validateMapping(EmisMetaData field, DbRowAccess access)
    {
        EmisMetaEnumTuple dimensions = field.getArrayDimensions(); 
        if (dimensions == null)
        {
            if (access instanceof DbRowArrayAccess)
                return false; 
            
            // Need to make sure value mapping is ok. 
            return validateMapping(access, field.getType(), field.getEnumType()); 
        }
        else
        {
            if (!(access instanceof DbRowArrayAccess))
                return false; 

            DbRowArrayAccess arrayAccess = (DbRowArrayAccess) access; 
            
            DbRowAccess[] indexAccesses = arrayAccess.getIndexAccess();
            EmisMetaEnum[] arrayEnums = dimensions.getEnums(); 
            if (dimensions.getDimensions() != indexAccesses.length)
                return false; 
            
            for (int i = 0; i < indexAccesses.length; i++) 
            {
                if (indexAccesses[i] instanceof DbRowByColumnIndexAccess)
                    return true;
                else
                {
                    if (!validateMapping(indexAccesses[i], EmisDataType.ENUM, arrayEnums[i])) 
                        return false; 
                }
            }
            
            if (arrayAccess.getValueAccess() instanceof DbRowByColumnAccess)
                return true; 
            else
                return validateMapping(arrayAccess.getValueAccess(), field.getType(), field.getEnumType()); 
        }
    }

    private boolean validateMapping(DbRowAccess access, EmisDataType type, EmisMetaEnum enumType)
    {
        if (enumType == null)
            return access instanceof DbRowFieldAccess || access instanceof DbRowConstAccess; 
        else 
        {
            if (null == NamedUtil.find(enumType.getName(), model.getEnums()))
                return false;
            
            if (access instanceof DbRowEnumAccess)
                return NamedUtil.sameName(((DbRowEnumAccess) access).getEnumType(), enumType);
            else if (access instanceof DbRowConstAccess)
                return -1 != enumType.getIndex(((DbRowConstAccess) access).getConstValue()); 
            
            return false; 
        }
    }
}

