package shapefile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Index {
	int [] offsets;
	int [] lengths;
	
	public void read (DataInput in, int n) throws IOException {
		offsets = new int [n];
		lengths = new int [n];
		for (int i = 0;i < n;i++) {
			offsets [i] = in.readInt ();
			lengths [i] = in.readInt ();
		}
	}

	public void write (DataOutput out,int n) throws IOException {
		for (int i = 0;i < offsets.length;i++) {
			out.writeInt (offsets [i]);
			out.writeInt (lengths [i]);
		}
	}

	public int getLength () { // in 16-bit words
		return offsets.length << 2;
	}
}
