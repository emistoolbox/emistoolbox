package shapefile.coordinates;

import java.util.List;

public class GeographicCoordinateSystem extends CoordinateSystemObject implements CoordinateSystem
{
    Datum datum;
    PrimeMeridian primeMeridian;
    Unit angularUnit;

    public GeographicCoordinateSystem(List<Object> components) {
        super(components, 4);
        this.datum = ((Datum) construct((WellKnownTextObject) components.get(1)));
        this.primeMeridian = ((PrimeMeridian) construct((WellKnownTextObject) components.get(2)));
        this.angularUnit = ((Unit) construct((WellKnownTextObject) components.get(3)));
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: shapefile.coordinates.GeographicCoordinateSystem JD-Core
 * Version: 0.6.0
 */