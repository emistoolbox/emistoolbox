package shapefile.coordinates;

import java.util.List;

public abstract class CoordinateSystemObject
{
    String name;

    protected CoordinateSystemObject(List<Object> components, int expectedLength) {
        this.name = ((String) components.get(0));
        if (components.size() < expectedLength)
            throw new Error("truncated coordinate system object");
        if (components.size() > expectedLength)
            throw new Error("optional coordinate system components not implemented");
    }

    public static CoordinateSystemObject construct(WellKnownTextObject wellKnownTextObject)
    {
        if (wellKnownTextObject.label.equals("GEOGCS"))
            return new GeographicCoordinateSystem(wellKnownTextObject.components);
        if (wellKnownTextObject.label.equals("DATUM"))
            return new Datum(wellKnownTextObject.components);
        if (wellKnownTextObject.label.equals("SPHEROID"))
            return new Spheroid(wellKnownTextObject.components);
        if (wellKnownTextObject.label.equals("PRIMEM"))
            return new PrimeMeridian(wellKnownTextObject.components);
        if (wellKnownTextObject.label.equals("UNIT"))
        {
            return new Unit(wellKnownTextObject.components);
        }
        throw new Error("unknown label in well-known text coordinate system object: " + wellKnownTextObject.label);
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: shapefile.coordinates.CoordinateSystemObject JD-Core Version:
 * 0.6.0
 */