package shapefile.coordinates;

import java.util.ArrayList;
import java.util.List;

public class ProjectedCoordinateSystem extends CoordinateSystemObject implements CoordinateSystem
{
	GeographicCoordinateSystem geographicCoordinateSystem;
	Projection projection;
	List<Parameter> parameters = new ArrayList<> ();
	Unit linearUnit;
	
	public ProjectedCoordinateSystem(List<Object> components) {
		super(components, components.size());
        this.geographicCoordinateSystem = ((GeographicCoordinateSystem) construct((WellKnownTextObject) components.get(1)));
        this.projection = ((Projection) construct((WellKnownTextObject) components.get(2)));
        for (int index = 3;;index++) {
        	CoordinateSystemObject coordinateSystemObject = construct((WellKnownTextObject) components.get (index));
        	if (coordinateSystemObject instanceof Parameter)
        		parameters.add ((Parameter) coordinateSystemObject);
        	else {
        		linearUnit = (Unit) coordinateSystemObject;
        		if (index != components.size () - 1)
                    throw new Error("optional projected coordinate system components not implemented");
        		break;
        	}
        }
	}
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: shapefile.coordinates.ProjectedCoordinateSystem JD-Core
 * Version: 0.6.0
 */