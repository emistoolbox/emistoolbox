package info.joriki.truetype;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import info.joriki.io.Util;
import info.joriki.sfnt.SFNTTable;
import info.joriki.util.InfiniteArrayList;

public abstract class AbstractGlyphTable extends SFNTTable {
	protected LocationTable locationTable;

	List outlines = new InfiniteArrayList ();

	public AbstractGlyphTable () {
	    super (GLYF);
	}

	public void writeTo (DataOutput out) throws IOException {
	    int [] offsets = new int [outlines.size () + 1];
	
	    for (int i = 0,offset = 0;i < outlines.size ();)
	      {
	    	byte [] data = getOutlineData (i);
	        out.write (data);
	        offset += data.length;
	        if ((offset & 1) != 0)
	          {
	            out.write (0);
	            offset++;
	          }
	        offsets [++i] = offset;
	      }
	
	    // we won't be needing the old one since we just fetched all the outlines.
	    locationTable = new LocationTable (offsets);
	  }

	public LocationTable getLocationTable () {
	    return locationTable;
	}

	abstract protected byte [] getOutlineData (int index);
}