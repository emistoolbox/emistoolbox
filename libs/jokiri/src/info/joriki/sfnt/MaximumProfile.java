/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.joriki.io.Outputable;

import info.joriki.util.Assertions;

public class MaximumProfile extends SFNTTable
{
  final static int SHORT = 0x00005000;
  final static int LONG  = 0x000010000;

  public int version;
  public int numGlyphs;
  public Extension extension;
  
  public static class Extension implements Outputable
  {
    public int maxPoints;
    public int maxContours;
    public int maxCompositePoints;
    public int maxCompositeContours;
    public int maxZones;
    public int maxTwilightPoints;
    public int maxStorage;
    public int maxFunctionDefs;
    public int maxInstructionDefs;
    public int maxStackElements;
    public int maxSizeOfInstructions;
    public int maxComponentElements;
    public int maxComponentDepth;

    Extension (DataInput in) throws IOException
    {
      maxPoints             = in.readUnsignedShort ();
      maxContours           = in.readUnsignedShort ();
      maxCompositePoints    = in.readUnsignedShort ();
      maxCompositeContours  = in.readUnsignedShort ();
      maxZones              = in.readUnsignedShort ();
      maxTwilightPoints     = in.readUnsignedShort ();
      maxStorage            = in.readUnsignedShort ();
      maxFunctionDefs       = in.readUnsignedShort ();
      maxInstructionDefs    = in.readUnsignedShort ();
      maxStackElements      = in.readUnsignedShort ();
      maxSizeOfInstructions = in.readUnsignedShort ();
      maxComponentElements  = in.readUnsignedShort ();
      maxComponentDepth     = in.readUnsignedShort ();
    }

    public void writeTo (DataOutput out) throws IOException
    {
      out.writeShort (maxPoints);
      out.writeShort (maxContours);
      out.writeShort (maxCompositePoints);
      out.writeShort (maxCompositeContours);
      out.writeShort (maxZones);
      out.writeShort (maxTwilightPoints);
      out.writeShort (maxStorage);
      out.writeShort (maxFunctionDefs);
      out.writeShort (maxInstructionDefs);
      out.writeShort (maxStackElements);
      out.writeShort (maxSizeOfInstructions);
      out.writeShort (maxComponentElements);
      out.writeShort (maxComponentDepth);
    }
    
    public void add (Extension extension) {
	    maxPoints = Math.max (maxPoints,extension.maxPoints);
	    maxContours = Math.max (maxContours,extension.maxContours);
	    maxCompositePoints = Math.max (maxCompositePoints,extension.maxCompositePoints);
	    maxCompositeContours = Math.max (maxCompositeContours,extension.maxCompositeContours);
	    maxZones = Math.max (maxZones,extension.maxZones);
	    maxTwilightPoints = Math.max (maxTwilightPoints,extension.maxTwilightPoints);
	    maxStorage = Math.max (maxStorage,extension.maxStorage);
	    maxFunctionDefs = Math.max (maxFunctionDefs,extension.maxFunctionDefs);
	    maxInstructionDefs = Math.max (maxInstructionDefs,extension.maxInstructionDefs);
	    maxStackElements = Math.max (maxStackElements,extension.maxStackElements);
	    maxSizeOfInstructions = Math.max (maxSizeOfInstructions,extension.maxSizeOfInstructions);
	    maxComponentElements = Math.max (maxComponentElements,extension.maxComponentElements);
	    maxComponentDepth = Math.max (maxComponentDepth,extension.maxComponentDepth);
    }
  }

  public MaximumProfile (DataInput in) throws IOException
  {
    super (MAXP);

    version = in.readInt ();
    numGlyphs = in.readUnsignedShort ();
    if (version == LONG)
      extension = new Extension (in);
    else
      Assertions.expect (version,SHORT);
  }

  public void writeTo (DataOutput out) throws IOException
  {
    version = extension == null ? SHORT : LONG;
    out.writeInt (version);
    out.writeShort (numGlyphs);
    if (extension != null)
      extension.writeTo (out);
  }
  
}
