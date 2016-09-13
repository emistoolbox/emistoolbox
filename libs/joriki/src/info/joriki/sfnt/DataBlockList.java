package info.joriki.sfnt;

import java.util.ArrayList;

public class DataBlockList extends ArrayList<DataBlock> {
	int offset;

	public boolean add (DataBlock dataBlock) {
        int index = indexOf (dataBlock);
        if (index < 0) {
        	dataBlock.offset = offset;
        	offset += dataBlock.data.length;
        	return super.add (dataBlock);
        }

        dataBlock.offset = get (index).offset;
        return false;
	}
}
