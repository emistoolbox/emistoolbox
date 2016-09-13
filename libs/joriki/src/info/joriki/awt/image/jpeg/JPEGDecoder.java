/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import java.io.IOException;

import info.joriki.awt.image.StreamingImageDecoder;

import info.joriki.util.Assertions;

public class JPEGDecoder extends StreamingImageDecoder
{
  JPEGTerminal terminal;
  JPEGUpSampler upSampler;
  JPEGReader reader;

  public JPEGDecoder ()
  {
    this (true);
  }

  public JPEGDecoder (boolean topDownLeftRight)
  {
    this (topDownLeftRight,true);
  }

  public JPEGDecoder (boolean topDownLeftRight,boolean linearInterpolation)
  {
    super (topDownLeftRight ? SIMPLE_HINTS : 0);

    terminal =
      topDownLeftRight ?
      (JPEGTerminal) new JPEGSender () :
      (JPEGTerminal) new JPEGEmitter ();
    
    upSampler = new JPEGUpSampler (linearInterpolation);
  }

  public void setSource (Object source)
  {
    if (source instanceof JPEGReader)
      reader = (JPEGReader) source;
    else
      super.setSource (source);
  }

  protected void produceStaticImage () throws IOException
  {
    Assertions.unexpect (reader == null,in == null);
    if (reader == null)
      reader = new JPEGReader (in);
    height = reader.height;
    width = reader.width;
    colorModel = reader.getColorModel ();

    setParameters ();

    JPEGConcatenator.concatenate (reader,upSampler,terminal).setSink (this);

    reader.scan ();
  }
}
