package com.emistoolbox.common;

import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import java.util.ArrayList;
import java.util.List;

public class FlatArrayUtil
{
	public static int getFlatIndex(byte[] indexes, byte[] sizes)
	{ return getFlatIndex(toInts(indexes), toInts(sizes)); }
	
	public static int[] toInts(byte[] values)
	{
		if (values == null)
			return null; 
		
		int[] result = new int[values.length]; 
		for (int i = 0; i < result.length; i++) 
			result[i] = values[i]; 
		
		return result; 
	}

	public static int getFlatIndex(int[] indexes, int[] sizes)
    {
        if (indexes.length != sizes.length)
        {
            throw new IllegalArgumentException("Number of indexes (" + indexes.length + ") doesn't match number of dimension sizes (" + sizes.length + ").");
        }

        int result = 0;
        for (int i = 0; i < sizes.length; i++)
        {
            if ((indexes[i] == -1) || (indexes[i] >= sizes[i]))
            {
                return -1;
            }
            result *= sizes[i];
            result += indexes[i];
        }

        return result;
    }

    public static int[] getIndexesFromFlatIndex(int index, int[] sizes)
    {
        if ((index < 0) || (index >= getArraySize(sizes)))
        {
            throw new IllegalArgumentException("Flat index '" + index + "' was out of range.");
        }

        int[] result = new int[sizes.length];

        for (int i = sizes.length - 1; i >= 0; i--)
        {
            result[i] = (index % sizes[i]);
            index /= sizes[i];
        }

        return result;
    }

    public static int getArraySize(int[] sizes)
    {
        int result = 1;
        for (int i = 0; i < sizes.length; i++)
        {
            result *= sizes[i];
        }
        return result;
    }

    public static List<Integer> getIndexes(EmisMetaEnumTuple dimensions, EmisContext context)
    {
        List result = new ArrayList();

        EmisMetaEnum[] enums = dimensions.getEnums();
        int[] sizes = new int[enums.length];
        EmisEnumSet[] enumValues = new EmisEnumSet[enums.length];
        for (int i = 0; i < enums.length; i++)
        {
            sizes[i] = enums[i].getSize();
            enumValues[i] = context.getEnumFilter(enums[i].getName());
        }

        int totalSize = getArraySize(sizes);
        for (int id = 0; id < totalSize; id++)
        {
            if (useIndex(id, enumValues, sizes))
            {
                result.add(Integer.valueOf(id));
            }
        }
        return result;
    }

    public static boolean useIndex(int id, EmisEnumSet[] enumValues, int[] sizes)
    {
        int[] indexes = getIndexesFromFlatIndex(id, sizes);
        for (int i = 0; i < indexes.length; i++)
        {
            if ((enumValues[i] != null) && (!enumValues[i].hasValue((byte) indexes[i])))
            {
                return false;
            }
        }
        return true;
    }
}
