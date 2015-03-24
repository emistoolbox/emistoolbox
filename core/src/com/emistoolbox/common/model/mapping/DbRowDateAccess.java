package com.emistoolbox.common.model.mapping;

import com.emistoolbox.common.model.mapping.impl.DbRowAccessBase;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnum;

import java.util.Map;
import java.util.Set;

public class DbRowDateAccess extends DbRowAccessBase implements DbRowAccess
{
    private static final long serialVersionUID = 1L;
    private EmisMetaDateEnum dateType;
    private DbRowAccess[] accesses;

    public DbRowDateAccess()
    { this(false); }
    
    public DbRowDateAccess(boolean withLoop)
    {}

    public String getValue(int valueIndex, Map<String, String> row)
    {
        return "" + getValueAsInt(valueIndex, -1, row);
    }

    public int getValueAsInt(int valueIndex, int defaultValue, Map<String, String> row)
    {
        EmisMetaEnum[] enums = this.dateType.getEnums();
        byte[] indexes = new byte[this.accesses.length];
        for (int i = 0; i < this.accesses.length; i++)
        {
            indexes[i] = getEnumIndex(this.accesses[i].getValue(valueIndex, row), enums[i]);
            if (indexes[i] == -1)
            	return -1;
        }
        
        return this.dateType.getIndex(indexes);
    }

    private byte getEnumIndex(String value, EmisMetaEnum meta)
    {
        if (value == null)
            return -1;
        try
        {
            return Byte.parseByte(value);
        }
        catch (NumberFormatException ex)
        {
        }
        return meta.getIndex(value);
    }

    public EmisMetaDateEnum getDateType()
    {
        return this.dateType;
    }

    public DbRowAccess[] getAccesses()
    {
        return this.accesses;
    }

    public void setAccesses(DbRowAccess[] accesses)
    {
        if (accesses.length != this.dateType.getDimensions())
            throw new IllegalArgumentException("Date enum " + this.dateType.getName() + " expects " + this.dateType.getDimensions() + " dimensions.");
        this.accesses = accesses;
    }

    public void setDateType(EmisMetaDateEnum dateType)
    {
        this.dateType = dateType;
        this.accesses = new DbRowAccess[dateType.getDimensions()];
    }

    public int getValuesPerRow()
    {
        return 1;
    }

    public void addColumns(Set<String> columns)
    {
        for (DbRowAccess access : this.accesses)
        {
            if (access != null)
                access.addColumns(columns);
        }
    }

    public void map(DbRowAccessFn callback)
    {
        super.map(callback); 
        DbRowAccessBase.map(callback,  accesses); 
    }

	@Override
	public String getInfo() 
	{
		StringBuffer result = new StringBuffer("date:"); 
		result.append(dateType.getName());
		result.append("("); 
		String delim = ""; 
		for (DbRowAccess access : accesses)
		{
			result.append(delim); 
			result.append(access.getInfo()); 
			delim = ","; 
		}
		
		result.append(")"); 
		
		return result.toString();
	}
}
