package shapefile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class PolyRecord extends BoundedShapefileRecord {
	int [] parts;
	
	public void read (DataInput in) throws IOException {
		super.read (in);
		int nparts = in.readInt ();
		int npoints = in.readInt ();
		parts = new int [nparts];
		for (int i = 0;i < nparts;i++)
			parts [i] = in.readInt ();
		points = new Point [npoints];
		for (int i = 0;i < npoints;i++) {
			points [i] = new Point ();
			points [i].read (in);
		}
	}

	public void write (DataOutput out) throws IOException {
		super.write (out);
		out.writeInt (parts.length);
		out.writeInt (points.length);
		for (int part : parts)
			out.writeInt (part);
		for (Point point : points)
			point.write (out);
	}
	
	public int getLength () {
		int length = super.getLength () + 4 + (parts.length << 1); // in 16-bit words
		for (Point point : points)
			length += point.getLength ();
		return length;
	}

	public String toDataString () {
		StringBuilder stringBuilder = new StringBuilder ();
		for (int index = 0,k = 0;k < parts.length;k++) {
			if (k != 0)
				stringBuilder.append (',');
			int limit = k == parts.length - 1 ? points.length : parts [k + 1];
			for (;index < limit;index++) {
				Point p = points [index];
				stringBuilder.append (toDataString (p.x)).append (',').append (toDataString (p.y)).append (',');
			}
		}

		if (stringBuilder.length () != 0)
			stringBuilder.setLength (stringBuilder.length () - 1);
		
		return stringBuilder.toString ();
	}

	protected void computeBoundingBox () {
		for (Point point : points)
			boundingBox.add (point);
	}
	
	public void setPoints (Point [] points) {
		super.setPoints (points);
		parts = new int [1]; // a single polygon/polyline, starting at index 0
	}
}
