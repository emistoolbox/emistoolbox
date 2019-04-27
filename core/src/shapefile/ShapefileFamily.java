package shapefile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import shapefile.coordinates.CoordinateSystem;
import shapefile.coordinates.CoordinateSystemObject;
import shapefile.coordinates.WellKnownTextObject;

import com.emistoolbox.server.mapping.DbDataSource;
import com.emistoolbox.server.mapping.DbResultSet;
import com.emistoolbox.server.mapping.DbResultSetBase;
import com.emistoolbox.server.mapping.SingleTableDataSource;

import xbase.DatabaseFile;

public class ShapefileFamily {
	final static String defaultProjectionString = "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.0174532925199433]]\000";

	IndexShapefile mainIndex = new IndexShapefile ();
	MainShapefile main = new MainShapefile (mainIndex.index);
//	MainSpatialFile spatial;
//	IndexSpatialFile spatialIndex;
	DatabaseFile database = new DatabaseFile ();
	CoordinateSystem sourceCoordinateSystem;

	public void read (String filename) throws IOException {
		mainIndex.read (filename + ".shx");
		main.read (filename + ".shp");
//		spatialIndex = new IndexSpatialFile ();
//		spatialIndex.read (filename + ".sbx");
//		spatial = new MainSpatialFile ();
//		spatial.read (filename + ".sbn");
		database.read (filename + ".dbf");
		BufferedReader reader = new BufferedReader (new FileReader (filename + ".prj"));
		sourceCoordinateSystem = (CoordinateSystem) CoordinateSystemObject.construct (WellKnownTextObject.parse (reader.readLine ()));
		reader.close ();
	}
	
	public void write (String filename) throws IOException {
		main.prepareWrite ();
		mainIndex.globalShapeType = main.globalShapeType;
		mainIndex.boundingBox = new BoundingBox (main.boundingBox);
		main.write (filename + ".shp");
		mainIndex.write (filename + ".shx");
		database.write (filename + ".dbf");
		FileWriter writer = new FileWriter (filename + ".prj");
		writer.write (defaultProjectionString);
		writer.close ();
	}

	private static String getShapeFieldSuffix (int shapeType) {
		switch (shapeType) {
		case ShapeTypes.Point      : return "point";
		case ShapeTypes.PolyLine   : return "polyline";
		case ShapeTypes.Polygon    : return "polygon";
		case ShapeTypes.MultiPoint : return "multipoint";
		case ShapeTypes.PointZ     : return "pointz";
		default : throw new Error ("shape type " + shapeType + " not implemented");
		}
	}
	
	public DbDataSource getDataSource() {
		List<String> fieldNames = new ArrayList<String> ();
		String shapeFieldName = "gis_" + getShapeFieldSuffix (main.globalShapeType);
		fieldNames.add (shapeFieldName);
		fieldNames.addAll (database.getFieldNames ());
		return new ShapefileDataSource(fieldNames,shapeFieldName);
	}
	
	public class ShapefileDataSource extends SingleTableDataSource {
		public static final String shapeTableName = "shapes";

		String shapeFieldName;

		CoordinateSystem targetCoordinateSystem = sourceCoordinateSystem;
		boolean needTransform = false;
		
		public ShapefileDataSource (List<String> fieldNames,String shapeFieldName) {
			super (shapeTableName,fieldNames);
			this.shapeFieldName = shapeFieldName;
		}
		
		public DbResultSet query(String query) {
			return new DbResultSetBase (fieldNames) {
				int index = -1;
				
				public boolean next () throws IOException {
					index++;
					return index < main.records.length; 
				}

				public String get (String key) throws IOException {
					return key.equals (shapeFieldName) ? main.records [index].toDataString () : database.records [index].fields [database.getFieldIndex (key)].toString ();
				}

				public void close () {}
			};
		}
		
		public void setTargetCoordinateSystem (CoordinateSystem coordinateSystem) {
			targetCoordinateSystem = coordinateSystem;
			needTransform = !targetCoordinateSystem.equals (sourceCoordinateSystem);
			if (needTransform)
				throw new Error ("coordinate transforms not implemented");
		}
		
		public CoordinateSystem getSourceCoordinateSystem () {
			return sourceCoordinateSystem;
		}
	}
}
