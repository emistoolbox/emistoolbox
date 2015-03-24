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
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.BitSet JD-Core Version: 0.6.0
 */