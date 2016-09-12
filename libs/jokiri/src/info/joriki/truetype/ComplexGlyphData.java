package info.joriki.truetype;

import info.joriki.io.Util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComplexGlyphData extends GlyphData implements ComplexGlyphSpeaker {
	static class Component {
		int flags;
		int glyphIndex;
		GlyphOutlineData glyphOutlineData;
		byte [] data;
		
		public Component (DataInput in) throws IOException {
	        flags = in.readShort ();
	        glyphIndex = in.readUnsignedShort ();
	        data = new byte [
	        		(((flags & ARGS_ARE_WORDS) == ARGS_ARE_WORDS) ? 4 : 2) +
	        		(((flags & HAS_TWO_BY_TWO) == HAS_TWO_BY_TWO) ? 8 :
	        		 ((flags & HAS_X_AND_Y_SCALE) == HAS_X_AND_Y_SCALE) ? 4 :
	        		 ((flags & HAS_SCALE) == HAS_SCALE) ? 2 : 0)
	        				 ];
	        in.readFully (data);
		}
		
		public void writeTo (DataOutput out) throws IOException {
			out.writeShort (flags);
			out.writeShort (glyphOutlineData.index);
			out.write (data);
		}
		
		public int getLength () {
			return data.length + 4;
		}
		
		public boolean equals (Object o) {
			if (!(o instanceof Component))
				return false;
			Component c = (Component) o;
			return c.flags == flags && c.glyphOutlineData.equals (glyphOutlineData) && Arrays.equals (c.data,data);
		}

		public void map (GlyphOutlineData [] map) {
			glyphOutlineData = map [glyphIndex];
		}
	}
		
	
	List<Component> components = new ArrayList<Component> ();
	byte [] instructions;
	
	public ComplexGlyphData (DataInput in,int nbytes) throws IOException {
	    boolean hasInstructions = false;
	    
	    Component component;
	    
	    do {
	    	component = new Component (in);
	        hasInstructions |= (component.flags & HAS_INSTRUCTIONS) == HAS_INSTRUCTIONS;
	        components.add (component);
	        nbytes -= component.getLength ();
	    } while ((component.flags & MORE_COMPONENTS) == MORE_COMPONENTS);

	    if (hasInstructions) {
	      instructions = Util.readBytes (in,in.readUnsignedShort ());
	      nbytes -= instructions.length + 2;
	    }
	    
	    // allow for alignment padding; see GlyphTable.getGlyphOutlineData
	    if (nbytes >= 4)
	    	throw new Error (nbytes + " bytes left");
	    while (nbytes-- > 0)
	    	if (in.readByte () != 0)
	    		throw new Error ("trailing garbage");
	  }
	
	public void writeTo (DataOutput out) throws IOException {
		for (Component component : components)
			component.writeTo (out);
		if (instructions != null) {
			out.writeShort (instructions.length);
			out.write (instructions);
		}
	}

	public boolean equals (Object o) {
		if (!(o instanceof ComplexGlyphData))
			return false;
		ComplexGlyphData data = (ComplexGlyphData) o;
		return data.components.equals (components) && Arrays.equals (data.instructions,instructions);
	}

	public void map (GlyphOutlineData [] map) {
		for (Component component : components)
			component.map (map);
	}
}
