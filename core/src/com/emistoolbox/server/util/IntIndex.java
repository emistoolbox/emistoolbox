package com.emistoolbox.server.util;

import java.util.Iterator;

public class IntIndex implements Iterable<int[]>
{
    int[] sizes;
    long flatSize = 1L;

    public IntIndex(int[] sizes) {
        this.sizes = sizes;
        for (int i = 0; i < sizes.length; i++)
            this.flatSize *= sizes[i];
    }

    public Iterator<int[]> iterator()
    {
        return new Iterator() {
            long nextFlatIndex = 0L;

            public boolean hasNext()
            {
                return this.nextFlatIndex < IntIndex.this.flatSize;
            }

            public int[] next()
            {
                return IntIndex.this.getIndexes(this.nextFlatIndex++);
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    private int[] getIndexes(long flatIndex)
    {
        int[] result = new int[this.sizes.length];
        for (int i = this.sizes.length - 1; i >= 0; i--)
        {
            result[i] = (int) (flatIndex % this.sizes[i]);
            flatIndex /= this.sizes[i];
        }

        return result;
    }
}
