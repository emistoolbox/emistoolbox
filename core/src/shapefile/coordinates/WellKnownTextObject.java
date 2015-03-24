package shapefile.coordinates;

import java.util.ArrayList;
import java.util.List;

public class WellKnownTextObject
{
    String label;
    List<Object> components;

    private WellKnownTextObject() {
    }

    public static WellKnownTextObject parse(final String string)
    {
        return new Object() {
            int pos;

            WellKnownTextObject parse()
            {
                WellKnownTextObject object = new WellKnownTextObject();
                int bracket = string.indexOf('[', pos);
                object.label = string.substring(pos, bracket).trim();
                object.components = new ArrayList<Object>();
                pos = bracket + 1;
                for (;;)
                {
                    switch (string.charAt(pos)) {
                    case ']':
                        pos++;
                        return object;
                    case '"':
                        pos++;
                        int closingQuote = string.indexOf('"', pos);
                        if (closingQuote == -1)
                            throw new Error("unterminated string in well-known text object");
                        object.components.add(string.substring(pos, closingQuote));
                        pos = closingQuote + 1;
                        break;
                    default:
                        for (int next = pos;; next++)
                        {
                            switch (string.charAt(next)) {
                            case '[':
                                object.components.add(parse());
                                break;
                            case ',':
                            case ']':
                                object.components.add(Double.parseDouble(string.substring(pos, next)));
                                pos = next;
                                break;
                            default:
                                continue;
                            }
                            break;
                        }
                    }
                    switch (string.charAt(pos)) {
                    case ',':
                        pos++;
                        break;
                    case ']':
                        break;
                    default:
                        throw new Error("invalid well-known text format");
                    }
                }
            }
        }.parse();
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        appendTo(builder);
        return builder.toString();
    }

    private void appendTo(StringBuilder builder)
    {
        builder.append(label).append('[');
        for (Object component : components)
        {
            if (component instanceof String)
                builder.append('"').append(component).append('"');
            else if (component instanceof Double)
                builder.append(component);
            else if (component instanceof WellKnownTextObject)
                ((WellKnownTextObject) component).appendTo(builder);
            else
                throw new InternalError("bad component in well-known text object");
            builder.append(',');
        }
        builder.setCharAt(builder.length() - 1, ']'); // instead of excess comma
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof WellKnownTextObject))
            return false;
        WellKnownTextObject wellKnownTextObject = (WellKnownTextObject) o;
        if (!(wellKnownTextObject.label.equals(label) && wellKnownTextObject.components.size() == components.size()))
            return false;
        for (int i = 0; i < components.size(); i++)
            if (!components.get(i).equals(wellKnownTextObject.components.get(i)))
                return false;
        return true;
    }
}
