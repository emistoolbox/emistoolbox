package shapefile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Range {
	public double min;
	public double max;

	public Range () {}
	
	public Range (Range range) {
		min = range.min;
		max = range.max;
	}

	public void read (DataInput in) throws IOException {
		min = in.readDouble ();
		max = in.readDouble ();
	}
	
	public void write (DataOutput out) throws IOException {
		out.writeDouble (min);
		out.writeDouble (max);
	}

	public boolean contains (double value) {
		return min <= value && value <= max;
	}
	
	public byte toByte (double value) {
// two attempts to handle rounding problems
//		return (byte) ((int) (254.999999999999985 * (value - min) / (max - min)) - 128);
//		return (byte) ((int) (255 * (value - min) / (max - min) - 1.5e-14) - 128);
		return (byte) ((int) (255 * (value - min) / (max - min)) - 128);
	}
	
	public String toString () {
		return "[" + min + "," + max + "]";
	}

	public boolean equals (Object o) {
		if (!(o instanceof Range))
			return false;
		Range range = (Range) o;
		return range.min == min && range.max == max;
	}
	
	public double length () {
		return max - min;
	}

	public double midpoint () {
		return (min + max) / 2;
	}

	public boolean contains (Range range) {
		return min <= range.min && range.max <= max;
	}

	public byte [] toByteBounds (Range range) {
		return new byte [] {range.toByte (min),(byte) Math.min (127,range.toByte (max) + 1)};
	}

	public void add (Range range) {
		min = Math.min (min,range.min);
		max = Math.max (max,range.max);
	}
}
