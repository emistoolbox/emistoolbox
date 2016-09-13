package info.joriki.truetype;

import java.io.DataOutput;
import java.io.IOException;

import info.joriki.io.Outputable;

abstract public class GlyphOutlineData implements Outputable {
	int index;
	short leftSideBearing;
	int advanceWidth;

	public GlyphOutlineData (int index,short leftSideBearing,int advanceWidth) {
		this.index = index;
		this.leftSideBearing = leftSideBearing;
		this.advanceWidth = advanceWidth;
	}

	public void writeTo (DataOutput out) throws IOException {}
	public void map (GlyphOutlineData [] map) {}
	
	public boolean equals (Object o) {
		if (!(o instanceof GlyphOutlineData))
			return false;
		GlyphOutlineData glyphOutlineData = (GlyphOutlineData) o;
		return glyphOutlineData.leftSideBearing == leftSideBearing && glyphOutlineData.advanceWidth == advanceWidth;
	}
}