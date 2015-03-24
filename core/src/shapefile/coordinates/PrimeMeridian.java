package shapefile.coordinates;

import java.util.List;

public class PrimeMeridian extends CoordinateSystemObject
{
    double longitude;

    public PrimeMeridian(List<Object> components) {
        super(components, 2);
        this.longitude = ((Double) components.get(1)).doubleValue();
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: shapefile.coordinates.PrimeMeridian JD-Core Version: 0.6.0
 */