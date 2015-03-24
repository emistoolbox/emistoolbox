package shapefile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Point {
	double x;
	double y;
	
	public void read (DataInput in) throws IOException {
		x = in.readDouble ();
		y = in.readDouble ();
	}

	public void write (DataOutput out) throws IOException {
		out.writeDouble (x);
		out.writeDouble (y);
	}

	public int getLength () {
		return 8; // in 16-bit words
	}
}
