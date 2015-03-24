package shapefile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class BoundingBox {
	Range [] ranges = new Range [2];
	
	public BoundingBox () {
		for (int i = 0;i < 2;i++)
			ranges [i] = new Range ();
	}
	
	public BoundingBox (BoundingBox box) {
		for (int i = 0;i < 2;i++)
			ranges [i] = new Range (box.ranges [i]);
	}
	
	public BoundingBox (Point point) {
		this ();
		ranges [0].min = ranges [0].max = point.x;
		ranges [1].min = ranges [1].max = point.y;
	}

	public static BoundingBox createEmptyBox () {
		BoundingBox emptyBox = new BoundingBox ();
		emptyBox.clear ();
		return emptyBox;
	}
	
	public void clear () {
		for (Range range : ranges) {
			range.min = Double.POSITIVE_INFINITY;
			range.max = Double.NEGATIVE_INFINITY;
		}
	}
	
	public void read (DataInput in) throws IOException {
		ranges [0].min = in.readDouble ();
		ranges [1].min = in.readDouble ();
		ranges [0].max = in.readDouble ();
		ranges [1].max = in.readDouble ();
	}
	
	public void write (DataOutput out) throws IOException {
		out.writeDouble (ranges [0].min);
		out.writeDouble (ranges [1].min);
		out.writeDouble (ranges [0].max);
		out.writeDouble (ranges [1].max);
	}
	
	public int getLength () {
		return 16; // in 16-bit words
	}

	public boolean contains (Point p) {
		return ranges [0].contains (p.x) && ranges [1].contains (p.y);
	}
	
	public String toString () {
		return ranges [0] + " x " + ranges [1];
	}
	
	public boolean equals (Object o) {
		if (!(o instanceof BoundingBox))
			return false;
		BoundingBox box = (BoundingBox) o;
		for (int i = 0;i < 2;i++)
			if (!box.ranges [i].equals (ranges [i]))
				return false;
		return true;
	}
	
	public boolean contains (BoundingBox boundingBox) {
		for (int i = 0;i < 2;i++)
			if (!ranges [i].contains (boundingBox.ranges [i]))
				return false;
		return true;
	}


	public void add (BoundingBox boundingBox) {
		for (int i = 0;i < 2;i++)
			ranges [i].add (boundingBox.ranges [i]);
	}


	public void add (Point point) {
		ranges [0].min = Math.min (ranges [0].min,point.x);
		ranges [0].max = Math.max (ranges [0].max,point.x);
		ranges [1].min = Math.min (ranges [1].min,point.y);
		ranges [1].max = Math.max (ranges [1].max,point.y);
	}

	public byte [] [] toByteBounds (BoundingBox referenceBox) {
		StringBuilder builder = new StringBuilder ();
		byte [] [] byteBounds = new byte [2] [];
		for (int i = 0;i < 2;i++)
			byteBounds [i] = ranges [i].toByteBounds (referenceBox.ranges [i]);
		for (int i = 0;i < 2;i++)
			for (int j = 0;j < 2;j++)
				builder.append (' ').append (byteBounds [i] [j]);
		return byteBounds;
	}
}
