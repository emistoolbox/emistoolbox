package shapefile;

import info.joriki.io.ReverseDataInputStream;
import info.joriki.io.ReverseDataOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

// specified in http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf
public abstract class Shapefile {
	final static int magic = 9994;
	final static int version = 1000;
	public static final int HEADER_LENGTH = 50; // in 16-bit words
	
	int fileLength;
	int globalShapeType;
	
	BoundingBox boundingBox = new BoundingBox ();
	Range zRange = new Range ();
	Range mRange = new Range ();
	
	public void read (String filename) throws IOException {
		BufferedInputStream in = new BufferedInputStream (new FileInputStream (filename));
		try {
			DataInputStream bigEndian = new DataInputStream (in);
			ReverseDataInputStream littleEndian = new ReverseDataInputStream (in);
			
			int actualMagic = bigEndian.readInt ();
			if (actualMagic != magic)
				throw new IOException ("expected magic number " + magic + " at beginning of shapefile header; found " + actualMagic);
			if (bigEndian.skipBytes (20) != 20)
				throw new IOException ("unexpected EOF on shapefile header");
			fileLength = bigEndian.readInt ();
			int actualVersion = littleEndian.readInt ();
			if (actualVersion != version)
				throw new IOException ("unknown version " + actualVersion + "; expected " + version);
			globalShapeType = littleEndian.readInt ();
			boundingBox.read (littleEndian);
			zRange.read (littleEndian);
			mRange.read (littleEndian);
			
			read (bigEndian,littleEndian);
		} finally { in.close (); }
	}
	
	public void write (String filename) throws IOException {
		BufferedOutputStream out = new BufferedOutputStream (new FileOutputStream (filename));
		DataOutputStream bigEndian = new DataOutputStream (out);
		ReverseDataOutputStream littleEndian = new ReverseDataOutputStream (out);
		
		bigEndian.writeInt (magic);
		bigEndian.write (new byte [20]);
		fileLength = HEADER_LENGTH + getLength (); // in 16-bit words
		bigEndian.writeInt (fileLength);
		littleEndian.writeInt (version);
		littleEndian.writeInt (globalShapeType);
		boundingBox.write (littleEndian);
		// these aren't being set anywhere currently
		zRange.write (littleEndian);
		mRange.write (littleEndian);
		
		write (bigEndian,littleEndian);

		out.close ();
	}
	
	protected abstract int getLength ();
	protected abstract void read (DataInput bigEndian,DataInput littleEndian) throws IOException;
	protected abstract void write (DataOutput bigEndian,DataOutput littleEndian) throws IOException;
}
