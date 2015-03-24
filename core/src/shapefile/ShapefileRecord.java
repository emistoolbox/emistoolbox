package shapefile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class ShapefileRecord {
	public int number;

	abstract public void read (DataInput in) throws IOException;
	abstract public void write (DataOutput in) throws IOException;
	abstract public int getLength ();
	abstract public BoundingBox getBoundingBox ();
	abstract public String toDataString ();
	abstract public int getRecordType ();
	
	public byte [] [] getByteBounds (BoundingBox referenceBox) {
		return getBoundingBox ().toByteBounds (referenceBox);
	}

	protected static String toDataString (double d) {
		return Long.toString (Double.doubleToLongBits (d),16);
	}
}
