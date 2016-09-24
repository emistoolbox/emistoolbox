/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

public class TextRenderingMode implements RenderingMode
{
  public final static int FILL = 0;
  public final static int STROKE = 1;
  public final static int BOTH = 2;
  public final static int NEITHER = 3;

  public final static int CLIP = 4;

  public final int mode;

  public TextRenderingMode (int mode)
  {
    this.mode = mode;
  }

  public boolean clips ()
  {
    return (mode & CLIP) != 0;
  }

  public boolean strokes ()
  {
    int fillStroke = mode & 3;
    return fillStroke == STROKE || fillStroke == BOTH;
  }

  public boolean fills ()
  {
    int fillStroke = mode & 3;
    return fillStroke == FILL || fillStroke == BOTH;
  }

  public boolean shows ()
  {
    return (mode & 3) != NEITHER;
  }
}
