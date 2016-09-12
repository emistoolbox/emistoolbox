package info.joriki.truetype;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class SimpleGlyphData extends GlyphData {
	byte [] data;
	
	public SimpleGlyphData (DataInput in,int nbytes) throws IOException {
		data = new byte [nbytes];
		in.readFully (data);
	}

	public void writeTo (DataOutput out) throws IOException {
		out.write (data);
	}
	
	public boolean equals (Object o) {
		return o instanceof SimpleGlyphData && Arrays.equals (data,((SimpleGlyphData) o).data);
	}
}
