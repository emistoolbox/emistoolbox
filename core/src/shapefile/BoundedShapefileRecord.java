package shapefile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

abstract class BoundedShapefileRecord extends ShapefileRecord {
	protected BoundingBox boundingBox = new BoundingBox ();
	protected Point [] points;

	public BoundingBox getBoundingBox () {
		boundingBox.clear ();
		computeBoundingBox ();
		return boundingBox;
	}

	public void read (DataInput in) throws IOException {
		boundingBox.read (in);
	}

	public void write (DataOutput out) throws IOException {
		boundingBox.write (out);
	}
	
	public int getLength () {
		return boundingBox.getLength ();
	}
	
	public void setPoints (Point [] points) {
		this.points = points;
	}

	protected abstract void computeBoundingBox ();
}
