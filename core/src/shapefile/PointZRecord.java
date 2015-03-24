package shapefile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PointZRecord extends PointRecord {
	double z;
	double m;
	
	public void read (DataInput in) throws IOException {
		super.read (in);
		z = in.readDouble ();
		m = in.readDouble ();
	}
	
	public void write (DataOutput out) throws IOException {
		super.write (out);
		out.writeDouble (z);
		out.writeDouble (m);
	}
	
	public int getLength () {
		return super.getLength () + 8; // in 16-bit words
	}
	
	public String toDataString () {
		return super.toDataString () + ',' + toDataString (z) + ',' + toDataString (m);
	}
	
	public int getRecordType () {
		return ShapeTypes.PointZ;
	}
}
