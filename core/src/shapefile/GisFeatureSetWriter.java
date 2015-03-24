package shapefile;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import xbase.CharacterField;
import xbase.DatabaseFile;
import xbase.FieldDescriptor;
import xbase.XBaseRecord;

import com.emistoolbox.server.renderer.gis.GisFeatureSet;

public class GisFeatureSetWriter {
	public void write (File dir,String baseName,GisFeatureSet features) throws IOException {
		write (new File (dir,baseName).getPath (),features);
	}
	
	private DecimalFormat decimalFormat = new DecimalFormat("#0.##"); 

	public void write (String path,GisFeatureSet features) throws IOException {
		int count = features.getCount ();

		String [] [] data = new String [count] [3];
		int [] lengths = new int[3];
		
		for (int i = 0;i < count;i++) 
		{
            String value = decimalFormat.format(features.getValue(i)); 
//			for (char c : value.toCharArray ())
//				if (c != '.' && c != '-' && !('0' <= c && c <= '9'))
//					throw new Error ("double value out of range" + value);
			if (value.length () > 18) // truncate without rounding
				value = value.substring (0,18);

			data[i][0] = Integer.toString(features.getId(i));
			data[i][1] = features.getTitle (i);

	        while (value.length () < lengths [2])
	            value += ' '; 
	        data[i][2] = value;

			for (int j = 0; j < 3; j++)
			    lengths[j] = Math.max (lengths [j],data [i] [j].length ());
		}
		     
	    ShapefileFamily shapefileFamily = new ShapefileFamily ();
		
		DatabaseFile database = shapefileFamily.database;
		
		database.fieldDescriptors = new FieldDescriptor [3];
		database.fieldDescriptors [0] = new FieldDescriptor ("ID"   ,'C',lengths [0]);
		database.fieldDescriptors [1] = new FieldDescriptor ("Title",'C',lengths [1]);
		database.fieldDescriptors [2] = new FieldDescriptor ("Value",'C',lengths [2]);
		
		database.records = new XBaseRecord [count];
		
		for (int i = 0;i < count;i++) {
			database.records [i] = new XBaseRecord (database.fieldDescriptors);
			for (int j = 0;j < 3;j++)
				((CharacterField) database.records [i].fields [j]).setString (data [i] [j]);
		}
		
		MainShapefile main = shapefileFamily.main;
		
		main.records = new ShapefileRecord [count];
		
		for (int i = 0;i < count;i++) {
			double [] feature = features.getFeature (i);
			int nanCount = 0;
			for (double f : feature)
				if (Double.isNaN (f))
					nanCount++;
			int nparts = nanCount + 1;
			int npoints = (feature.length - nanCount) >> 1;
			if (feature.length != (npoints << 1) + nanCount)
				throw new Error ("even number of coordinates expected");
			if (npoints == 1) {
				if (nparts != 1)
					throw new Error ("unexpected empty parts");
				PointRecord pointRecord = new PointRecord ();
				pointRecord.point.x = feature [0];
				pointRecord.point.y = feature [1];
				main.records [i] = pointRecord;
			}
			else {
				Point [] points = new Point [npoints];
				int [] parts = new int [nparts];
				int k = 0;
				npoints = 0;
				nparts = 0;
				outer:
				for (;;) {
					parts [nparts++] = npoints;
					for (;;) {
						if (k == feature.length)
							break outer;
						if (Double.isNaN (feature [k])) {
							k++;
							break;
						}
						Point p = new Point ();
						p.x = feature [k++];
						p.y = feature [k++];
						points [npoints++] = p;
					}
				}
				PolygonRecord record = new PolygonRecord ();
				record.setPoints (points);
				record.parts = parts;
				main.records [i] = record;
			}
		}
		
		shapefileFamily.write (path);
	}	
}
