package com.emistoolbox.common.model.meta;

public abstract interface EmisMetaEnumTuple
{
	public int findEnumPosition(EmisMetaEnum e); 
	
    public EmisMetaEnum[] getEnums();
    
    public byte[] getSizes(); 

    public void setEnums(EmisMetaEnum[] paramArrayOfEmisMetaEnum);

    public byte getDimensions();

    public int getCombinations();

    public int getIndex(byte[] paramArrayOfByte);

    public int getIndex(String[] paramArrayOfString);

    public EmisMetaEnum getMetaEnum();
}
