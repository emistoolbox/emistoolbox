package shapefile.coordinates;

import java.util.List;

public class Datum extends CoordinateSystemObject
{
    Spheroid spheroid;

    public Datum(List<Object> components) {
        super(components, 2);
        this.spheroid = ((Spheroid) construct((WellKnownTextObject) components.get(1)));
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: shapefile.coordinates.Datum JD-Core Version: 0.6.0
 */