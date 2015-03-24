package xbase;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface XBaseField {
	void read (DataInput in) throws IOException;
	void write (DataOutput out) throws IOException;
}
