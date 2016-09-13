package info.joriki.truetype;

public class EmptyGlyphOutlineData extends GlyphOutlineData {
	public EmptyGlyphOutlineData (int index,short leftSideBearing,int advanceWidth) {
		super (index,leftSideBearing,advanceWidth);
	}
	
	public boolean equals (Object o) {
		return o instanceof EmptyGlyphOutlineData && super.equals (o);
	}
}
