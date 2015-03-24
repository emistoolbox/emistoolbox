package shapefile.coordinates;

import java.util.List;

public class Unit extends CoordinateSystemObject
{
    double conversionFactor;

    protected Unit(List<Object> components) {
        super(components, 2);
        this.conversionFactor = ((Double) components.get(1)).doubleValue();
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: shapefile.coordinates.Unit JD-Core Version: 0.6.0
 */