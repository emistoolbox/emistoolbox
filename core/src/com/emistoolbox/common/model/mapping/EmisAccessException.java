package com.emistoolbox.common.model.mapping;

public class EmisAccessException extends Exception 
{
	private DbRowAccess access; 
	
	public EmisAccessException(DbRowAccess access, String message)
	{
		super(message); 
		this.access = access; 
	}

	public DbRowAccess getAccess()
	{ return access; } 
}
