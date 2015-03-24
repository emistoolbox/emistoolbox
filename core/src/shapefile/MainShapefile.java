package shapefile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MainShapefile extends Shapefile {
	Index index;
	ShapefileRecord [] records;

	public MainShapefile (Index index) {
		this.index = index;
	}
	
	protected void read (DataInput bigEndian,DataInput littleEndian) throws IOException {
		records = new ShapefileRecord [index.offsets.length];
		for (int i = 0;i < records.length;i++) {
			int number = bigEndian.readInt ();
			if (number != i + 1)
				throw new Error ("expected consecutive numbering");
			int length = bigEndian.readInt ();
			if (length != index.lengths [i])
				throw new Error ("record length mismatch");
			int shapeType = littleEndian.readInt ();
			if (shapeType != globalShapeType)
				throw new Error ("shape type mismatch");
			records [i] = createShapefileRecord (shapeType);
			records [i].read (littleEndian);
			records [i].number = number;
		}
	}

	private static ShapefileRecord createShapefileRecord (int shapeType) {
		switch (shapeType) {
		case ShapeTypes.Point      : return new PointRecord ();
		case ShapeTypes.PolyLine   : return new PolyLineRecord ();
		case ShapeTypes.Polygon    : return new PolygonRecord ();
		case ShapeTypes.MultiPoint : return new MultiPointRecord ();
		case ShapeTypes.PointZ     : return new PointZRecord ();
		default : throw new Error ("shape type " + shapeType + " not implemented");
		}
	}

	protected void write (DataOutput bigEndian,DataOutput littleEndian) throws IOException {
		index.offsets = new int [records.length];
		index.lengths = new int [records.length];
		int pos = HEADER_LENGTH; // in 16-bit words;
		for (int i = 0;i < records.length;i++) {
			int length = records [i].getLength () + 2; // in 16-bit words, including type but not index and length
			index.offsets [i] = pos;
			index.lengths [i] = length; 
			pos += length + 4; // in 16-bit words, additionally including index and length 
			bigEndian.writeInt (i + 1);
			bigEndian.writeInt (length);
			littleEndian.writeInt (globalShapeType);
			records [i].write (littleEndian);
		}
	}
	
	protected int getLength () {
		int length = 0;
		for (ShapefileRecord record : records)
			length += 6 + record.getLength (); // in 16-bit words
		return length;
	}

	// determine bounding box and global shape type
	public void prepareWrite () {
		boundingBox.clear ();
		globalShapeType = records [0].getRecordType ();
		for (ShapefileRecord record : records) {
			boundingBox.add (record.getBoundingBox ());
			if (record.getRecordType () != globalShapeType)
				throw new Error ();
		}
	}
}
