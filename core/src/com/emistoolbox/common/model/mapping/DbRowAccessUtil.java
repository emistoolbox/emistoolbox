package com.emistoolbox.common.model.mapping;

import java.util.HashSet;
import java.util.Set;

import com.emistoolbox.common.model.mapping.impl.DbRowEnumAccess;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.util.NamedUtil;

public class DbRowAccessUtil
{
    public static boolean isValid(DbRowAccess access, EmisMetaData field)
    {
        if (field == null)
            return false; 

        EmisMetaEnumTuple dims = field.getArrayDimensions(); 
        if (dims == null || dims.getDimensions() == 0)
            return (access instanceof DbRowFieldAccess) && field.getName().equals(((DbRowFieldAccess) access).getFieldName()); 

        if (!(access instanceof DbRowArrayAccess))
            return false;  

        EmisMetaEnum enums[] = dims.getEnums(); 

        // We have an array - verify each dimension
        for (int i = 0; i < dims.getDimensions(); i++)
        {
            EmisMetaEnum e = enums[i]; 
            if (-1 == findIndex(enums[i], (DbRowArrayAccess) access))
                return false;  
        }

        return true; 
    }
    
    private static int findIndex(EmisMetaEnum e, DbRowArrayAccess access)
    {
        DbRowAccess[] accesses = access.getIndexAccess(); 
        for (int i = 0 ; i < accesses.length; i++) 
        {
            if (accesses[i] instanceof DbRowEnumAccess)
            {
                if (NamedUtil.sameName(((DbRowEnumAccess) accesses[i]).getEnumType(), e))
                    return i; 
            }
            else if (accesses[i] instanceof DbRowMultipleAccess)
            {
                Set<String> values = new HashSet<String>(); 
                for (String index : ((DbRowMultipleAccess) accesses[i]).getIndexes())
                    if (values.contains(index))
                        values.remove(index); 
                        
                if (values.size() == 0)
                    return i; 
                    
            }   
        }
                
        return -1; 
    }
}
