package xbase;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CharacterField implements XBaseField {
	static Set<String> warnings = new HashSet<String> ();
	
	byte [] chars;

	protected CharacterField (FieldDescriptor fieldDescriptor) {
		chars = new byte [fieldDescriptor.length];
	}

	public void read (DataInput in) throws IOException {
		in.readFully (chars);
		if (!validate ()) {
			for (byte b : chars)
				if ((b != ' ' && b != '*') || b != chars [0])
					throw new IOException ("invalid field format: " + getClass ().getName () + " <" + this + ">");
			String warning = "warning: empty field: " + getClass ().getName ();
			if (warnings.add (warning))
				System.err.println (warning);
		}
	}

	public void write (DataOutput out) throws IOException {
		out.write (chars);
	}

	public String toString () {
		return new String (chars);
	}

	protected boolean validate () {
		return true;
	}

	public void setString (String string) {
		byte [] bytes = string.getBytes ();
		if (bytes.length > chars.length)
			throw new Error ("string length exceeds limits of character field");
		System.arraycopy (bytes,0,chars,0,bytes.length);
	}
}
