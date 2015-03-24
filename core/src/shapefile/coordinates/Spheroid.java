package shapefile.coordinates;

import java.util.List;

public class Spheroid extends CoordinateSystemObject
{
    double semiMajorAxis;
    double inverseFlattening;

    public Spheroid(List<Object> components) {
        super(components, 3);
        this.semiMajorAxis = ((Double) components.get(1)).doubleValue();
        this.inverseFlattening = ((Double) components.get(2)).doubleValue();
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: shapefile.coordinates.Spheroid JD-Core Version: 0.6.0
 */