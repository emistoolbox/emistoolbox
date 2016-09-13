/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import java.io.IOException;

public abstract class StaticImageDecoder extends ImageDecoder
{
  public StaticImageDecoder ()
  {
    this (SIMPLE_HINTS);
  }

  public StaticImageDecoder (int hints)
  {
    this.hints = hints;
  }

  protected void produceImage () throws IOException
  {
    produceStaticImage ();
    staticImageDone ();
  }

  abstract protected void produceStaticImage () throws IOException;
}
