package shapefile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PointRecord extends ShapefileRecord {
	Point point = new Point ();

	public void read (DataInput in) throws IOException {
		point.read (in);
	}

	public void write (DataOutput out) throws IOException {
		point.write (out);
	}
	
	public int getLength () {
		return point.getLength ();
	}

	public BoundingBox getBoundingBox () {
		return new BoundingBox (point);
	}

	public String toDataString () {
		return toDataString (point.x) + ',' + toDataString (point.y);
	}
	
	public int getRecordType () {
		return ShapeTypes.Point;
	}
}
