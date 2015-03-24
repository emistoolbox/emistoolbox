package xbase;

import info.joriki.io.ReverseDataOutputStream;

import java.io.DataInput;
import java.io.IOException;

public class XBaseRecord {
	public XBaseField [] fields;
	
	public XBaseRecord (FieldDescriptor [] fieldDescriptors) {
		fields = new XBaseField [fieldDescriptors.length];
		for (int i = 0;i < fields.length;i++)
			fields [i] = createField (fieldDescriptors [i]);
	}
	
	private XBaseField createField (FieldDescriptor fieldDescriptor) {
		switch (fieldDescriptor.type) {
		case 'C' : return new CharacterField (fieldDescriptor);
		case 'D' : return new DateField (fieldDescriptor);
		case 'F' : return new FloatingPointField (fieldDescriptor);
		case 'N' : return new NumberField (fieldDescriptor);
		default : throw new Error ("unknown field type: " + fieldDescriptor.type);
		}
	}
	
	public void read (DataInput in) throws IOException {
		int deleted = in.readUnsignedByte ();
		if (deleted != ' ')
			throw new Error ("deleted fields not implemented");
		
		for (XBaseField field : fields)
			field.read (in);
	}
	

	public void write (ReverseDataOutputStream out) throws IOException {
		out.writeByte (' ');
		for (XBaseField field : fields)
			field.write (out);
	}
	
	public String toString () {
		StringBuilder recordBuilder = new StringBuilder ();
		for (XBaseField field : fields) {
			recordBuilder.append (recordBuilder.length () == 0 ? '[' : ',');
			recordBuilder.append (field);
		}
		recordBuilder.append (']');
		return recordBuilder.toString ();
	}
}
