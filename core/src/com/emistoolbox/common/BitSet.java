package com.emistoolbox.common;

import java.io.Serializable;

public class BitSet implements Serializable
{
    private static final long serialVersionUID = 1L;
    private boolean[] values;

    public BitSet() {
    }

    public BitSet(int size) {
        this.values = new boolean[size];
    }

    public void clear()
    {
        for (int i = 0; i < this.values.length; i++)
            this.values[i] = false;
    }

    public void clear(int index)
    {
        this.values[index] = false;
    }

    public boolean get(int index)
    {
        return this.values[index];
    }

    public void set(int index)
    {
        this.values[index] = true;
    }

    public void set(int index, boolean value)
    {
        this.values[index] = value;
    }

    public int nextSetBit(int index)
    {
        while ((index < this.values.length) && (!this.values[index]))
        {
            index++;
        }
        if (index == this.values.length)
        {
            return -1;
        }
        return index;
    }
    
    public void and(BitSet bits)
    {
    	if (values.length != bits.values.length)
    		return; 
    	
    	for (int i = 0; i < values.length; i++)
    		values[i] = values[i] && bits.values[i]; 
    }
    
    public void or(BitSet bits)
    {
    	if (values.length != bits.values.length)
    		return; 
    	
    	for (int i = 0; i < values.length; i++)
    		values[i] = values[i] || bits.values[i]; 
    }
    
    public void not()
    { 
    	for (int i = 0; i < values.length; i++)
    		values[i] = !values[i]; 
    }
    
    public BitSet createCopy()
    {
    	BitSet result = new BitSet(values.length);
    	result.values = new boolean[values.length];
    	for (int i = 0; i < values.length; i++)
    		result.values[i] = values[i]; 

    	return result; 
    }
}
