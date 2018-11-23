package shapefile.coordinates;

import java.util.List;

public class Parameter extends CoordinateSystemObject {
	double value;
	
	public Parameter(List<Object> components) {
		super(components, 2);
		this.value = ((Double) components.get(1)).doubleValue();
	}
}
