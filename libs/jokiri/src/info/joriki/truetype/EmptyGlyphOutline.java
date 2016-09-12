/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.truetype;

import java.io.DataOutput;

public class EmptyGlyphOutline extends GlyphOutline
{
  public static final EmptyGlyphOutline
    emptyGlyphOutline = new EmptyGlyphOutline ();
  private EmptyGlyphOutline () {}
  public void writeTo (DataOutput out) {}
  public void interpret (ByteCodeInterpreter interpreter) {}
}
