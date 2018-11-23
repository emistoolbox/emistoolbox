package shapefile.coordinates;

import java.util.List;

public class Parameter extends CoordinateSystemObject {
	double value;
	
	public Parameter(List<Object> components) {
		super(components, 2);
		this.value = ((Double) components.get(1)).doubleValue();
	}
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: shapefile.coordinates.Parameter JD-Core
 * Version: 0.6.0
 */