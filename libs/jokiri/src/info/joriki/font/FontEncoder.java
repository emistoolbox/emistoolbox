/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.font;

import info.joriki.graphics.Point;

public interface FontEncoder
{
  void setFontBBox (double [] fontBBox);
  void setFontMatrix (double [] fontMatrix);
  void setWidths (Widths widths);
  void encodeGlyph (GlyphProvider glyphProvider,String glyphName,
                    int unicode,double width,Point position);
}
