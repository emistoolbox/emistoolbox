package shapefile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MultiPointRecord extends BoundedShapefileRecord {
	public void read (DataInput in) throws IOException {
		super.read (in);
		int n = in.readInt ();
		points = new Point [n];
		for (int i = 0;i < n;i++) {
			points [i] = new Point ();
			points [i].read (in);
		}
	}

	public void write (DataOutput out) throws IOException {
		super.write (out);
		out.writeInt (points.length);
		for (Point p : points)
			p.write (out);
	}

	public int getLength () {
		int length = super.getLength () + 2; // in 16-bit words
		for (Point p : points)
			length += p.getLength ();
		return length;
	}
	
	public String toDataString () {
		StringBuilder builder = new StringBuilder ();
		for (Point point : points)
			builder.append (toDataString (point.x)).append (',').append (toDataString (point.y)).append (',');
		if (points.length > 0)
			builder.setLength (builder.length () - 1);
		return builder.toString ();
	}
	
	public int getRecordType () {
		return ShapeTypes.MultiPoint;
	}

	protected void computeBoundingBox () {
		for (Point point : points)
			boundingBox.add (point);
	}
}
