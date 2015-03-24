package shapefile;

import java.io.File;
import java.io.IOException;

import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.server.renderer.gis.GisFeatureSet;

public class GisFeatureSetReader {
	public GisFeatureSet read (File dir,String baseName) throws IOException {
		return read (new File (dir,baseName).getPath ());
	}

	public GisFeatureSet read (String path) throws IOException {
		final ShapefileFamily shapefileFamily = new ShapefileFamily ();
		shapefileFamily.read (path);
		return new GisFeatureSet () {
			
			public double getValue (int index) 
			{ return index; }
			
			@Override
            public EmisIndicator getIndicator()
            { return null; }

            public String getTitle (int index) 
            { return Integer.toString (index); }
			
			public int getId (int index) 
			{ return index; }
			
			public double [] getFeature (int index) {
				ShapefileRecord shapefileRecord = shapefileFamily.main.records [index];
				if (shapefileRecord instanceof PointRecord) {
					Point point = ((PointRecord) shapefileRecord).point;
					return new double [] {point.x,point.y};
				}
				else if (shapefileRecord instanceof MultiPointRecord) {
					Point [] points = ((MultiPointRecord) shapefileRecord).points;
					double [] feature = new double [(points.length << 1)];
					int k = 0;
					for (Point point : points) {
						feature [k++] = point.x;
						feature [k++] = point.y;
					}
					return feature;
				}
				else if (shapefileRecord instanceof PolyRecord) {
					PolyRecord polyRecord = (PolyRecord) shapefileRecord;
					Point [] points = polyRecord.points;
					int [] parts = polyRecord.parts;
					double [] feature = new double [(points.length << 1) + parts.length - 1];
					int k = 0;
					int part = 0;
					for (int i = 0;i < points.length;i++) {
						if (i == parts [part]) {
							if (i != 0)
								feature [k++] = Double.NaN;
							if (part != parts.length - 1)
								part++;
						}
						feature [k++] = points [i].x;
						feature [k++] = points [i].y;
					}
					return feature;
				}
				else
					throw new Error ("shapefile record type " + shapefileRecord.getClass () + " not implemented");
			}

			public int getCount () {
				return shapefileFamily.main.records.length;
			}
			
			public EmisMetaEntity getEntityType () {
				throw new Error ("not implemented");
			}
			
			public double [] getBoundary (double [] values) {
				throw new Error ("not implemented");
			}
			
			public double [] getBoundary () {
				throw new Error ("not implemented");
			}
			
			public void add (int id,double [] coords,double value,String title) {
				throw new Error ("not implemented");
			}
		};
	}
}
