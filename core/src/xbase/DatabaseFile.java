package xbase;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import info.joriki.io.ReverseDataInputStream;
import info.joriki.io.ReverseDataOutputStream;

// format described in http://www.clicketyclick.dk/databases/xbase/format/dbf.html
public class DatabaseFile {
	final static int version = 3;
	
	int year;
	int month;
	int day;
	
	int headerLength;
	int recordLength;
	
	int languageDriver;

	public FieldDescriptor [] fieldDescriptors;
	public XBaseRecord [] records;
	
	Map<String,Integer> fieldIndices = new HashMap<String,Integer> ();
	
	public void read (String filename) throws IOException {
		ReverseDataInputStream in = new ReverseDataInputStream (new BufferedInputStream (new FileInputStream (filename)));
		try {
			// 0 : version
			int actualVersion = in.readUnsignedByte ();
			if (actualVersion != version)
				throw new Error ("unexpected xBase file version: " + actualVersion);
			// 1-3 : date of last update
			year = in.readUnsignedByte () + 1900;
			month = in.readUnsignedByte ();
			day = in.readUnsignedByte ();
			// 4-7 : number of records
			int nrecords = in.readInt ();
			// 8-9 : header length
			headerLength = in.readUnsignedShort ();
			// A-B : record length
			recordLength = in.readUnsignedShort ();
			// C-D : reserved
			int reserved = in.readUnsignedShort ();
			if (reserved != 0)
				throw new Error ("unexpected data in reserved field: " + reserved);
			// E : incompete transaction flag
			int incomplete = in.readUnsignedByte ();
			if (incomplete != 0)
				throw new Error ("incomplete database transaction");
			// F : encryption flag
			int encrypted = in.readUnsignedByte ();
			if (encrypted != 0)
				throw new Error ("encrypted database file");
			// 10-1B : reserved
			byte [] zeros = new byte [12];
			in.readFully (zeros);
			if (!Arrays.equals (zeros,new byte [zeros.length]))
				throw new Error ("unexpected data in reserved field");
			// 1C : MDX flag
			int mdx = in.readUnsignedByte ();
			if (mdx != 0)
				throw new Error ("MDX flag set");
			// 1D : language driver
			languageDriver = in.readUnsignedByte ();
			// 1E-1F : reserved
			reserved = in.readUnsignedShort ();
			if (reserved != 0)
				throw new Error ("unexpected data in reserved field: " + reserved);
			
			int nfields = (headerLength - 0x21) / 0x20;
			if (nfields * 0x20 + 0x21 != headerLength)
				throw new Error ("invalid header length");
			fieldDescriptors = new FieldDescriptor [nfields];
			for (int i = 0;i < nfields;i++) {
				fieldDescriptors [i] = new FieldDescriptor ();
				fieldDescriptors [i].read (in);
				fieldIndices.put (fieldDescriptors [i].toString (),i);
			}
			int terminator = in.readUnsignedByte ();
			if (terminator != 13)
				throw new Error ("unexpected terminator: " + terminator);
			records = new XBaseRecord [nrecords];
			for (int i = 0;i < nrecords;i++) {
				records [i] = new XBaseRecord (fieldDescriptors);
				records [i].read (in);
			}

// Some files don't contain this terminating byte
//			terminator = in.readUnsignedByte ();
//			if (terminator != 26)
//				throw new Error ("unexpected terminator: " + terminator);

		} finally { in.close (); }
	}
	
	public void write (String filename) throws IOException {
		ReverseDataOutputStream out = new ReverseDataOutputStream (new BufferedOutputStream (new FileOutputStream (filename)));
		try {
			// 0 : version
			out.writeByte (version);
			// 1-3 : date of last update
			Calendar calendar = Calendar.getInstance ();
			out.writeByte (calendar.get (Calendar.YEAR) - 1900);
			out.writeByte (calendar.get (Calendar.MONTH));
			out.writeByte (calendar.get (Calendar.DAY_OF_MONTH));
			// 4-7 : number of records
			out.writeInt (records.length);
			// 8-9 : header length
			out.writeShort (fieldDescriptors.length * 0x20 + 0x21);
			// A-B : record length
			out.writeShort (getRecordLength ());
			// C-D : reserved
			out.writeShort (0);
			// E : incomplete transaction flag
			out.writeByte (0);
			// F : encryption flag
			out.writeByte (0);
			// 10-1B : reserved
			out.write (new byte [12]);
			// 1C : MDX flag
			out.writeByte (0);
			// 1D : language driver
			out.writeByte (languageDriver);
			// 1E-1F : reserved
			out.writeShort (0);
			for (FieldDescriptor fieldDescriptor : fieldDescriptors)
				fieldDescriptor.write (out);
			out.writeByte (13);
			for (XBaseRecord record : records)
				record.write (out);
			out.writeByte (26);
		} finally { out.close (); }
	}
	
	public Set<String> getFieldNames () {
		return fieldIndices.keySet ();
	}
	
	public int getFieldIndex (String fieldName) {
		return fieldIndices.get (fieldName);
	}
	
	public int getRecordLength () {
		int sum = 1;
		for (FieldDescriptor fieldDescriptor : fieldDescriptors)
			sum += fieldDescriptor.length;
		return sum;
	}
}
