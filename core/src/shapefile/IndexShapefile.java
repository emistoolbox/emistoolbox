package shapefile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IndexShapefile extends Shapefile {
	Index index = new Index ();
	
	protected void read (DataInput bigEndian,DataInput littleEndian) throws IOException {
		index.read (bigEndian,(fileLength - HEADER_LENGTH) >> 2);
	}

	protected void write (DataOutput bigEndian,DataOutput littleEndian) throws IOException {
		index.write (bigEndian,(fileLength - HEADER_LENGTH) >> 2);
	}
	
	public int getLength () {
		return index.getLength ();
	}
}
