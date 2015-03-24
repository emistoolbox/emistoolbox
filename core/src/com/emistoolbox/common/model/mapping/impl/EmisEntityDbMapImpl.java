package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowDateAccess;
import com.emistoolbox.common.model.mapping.EmisEntityDbMap;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EmisEntityDbMapImpl extends EntityBaseDbMapImpl implements EmisEntityDbMap, Serializable
{
    private static final long serialVersionUID = 1L;
    private DbRowDateAccess dateAccess;
    private EmisMetaDateEnum dateEnum;
    private Map<String, DbRowAccess> fields = new HashMap<String, DbRowAccess>();

    public Map<String, DbRowAccess> getFieldAccess()
    { return this.fields; }

    public void setFieldAccess(Map<String, DbRowAccess> access)
    { this.fields = access; }

    public String toString()
    { return super.toString() + ", Date " + this.dateEnum.getName() + ", Fields: " + getFieldNames(); }

    public String getFieldNames()
    {
        StringBuffer result = new StringBuffer();
        for (String field : this.fields.keySet())
        {
            if (result.length() > 0)
                result.append(", ");
            result.append(field);
        }

        return result.toString();
    }

    public DbRowDateAccess getDateAccess()
    {
        return this.dateAccess;
    }

    public EmisMetaDateEnum getDateEnum()
    {
        return this.dateEnum;
    }

    public void setDateEnum(EmisMetaDateEnum dateEnum)
    {
        this.dateEnum = dateEnum;
    }

    public void setDateAccess(DbRowDateAccess access)
    { this.dateAccess = access; }

	public void updateDimensions() 
	{
		super.updateDimensions(); 
		DbRowAccessBase.updateDimensions(dateAccess);
		for (DbRowAccess field : fields.values())
			DbRowAccessBase.updateDimensions(field);
	}
}
