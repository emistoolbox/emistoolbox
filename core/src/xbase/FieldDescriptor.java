package xbase;

import info.joriki.io.ReverseDataOutputStream;

import java.io.DataInput;
import java.io.IOException;

public class FieldDescriptor {
	byte [] name = new byte [11];
	char type;
	int memoryAddress;
	int length;
	int decimalCount; // this is sometimes wrong, so we ignore it
	byte [] multiUserFields = new byte [14];
	
	public FieldDescriptor () {}
	
	public FieldDescriptor (String name,char type,int length) {
		byte [] bytes = name.getBytes ();
		System.arraycopy (bytes,0,this.name,0,bytes.length);
		this.type = type;
		this.length = length;
	}
	
	public void read (DataInput in) throws IOException {
		in.readFully (name);
		type = (char) in.readUnsignedByte ();
		memoryAddress = in.readInt (); // ignored
		length = in.readUnsignedByte ();
		decimalCount = in.readUnsignedByte ();
		in.readFully (multiUserFields); // ignored
	}

	public void write (ReverseDataOutputStream out) throws IOException {
		out.write (name);
		out.writeByte (type);
		out.writeInt (memoryAddress);
		out.writeByte (length);
		out.writeByte (decimalCount);
		out.write (multiUserFields);
	}
	
	public String toString () {
		for (int length = 0;;length++)
			if (length == name.length || name [length] == 0)
				return new String (name,0,length);
	}
}
