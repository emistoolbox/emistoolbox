/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.util.Hashtable;

public abstract class AbstractImageConsumer implements ImageConsumer, ImageProperties
{
  protected Hashtable properties = null;
  protected ColorModel colorModel = null;
  protected int width = -1;
  protected int height = -1;
  protected int hints = 0;

  public void setColorModel (ColorModel colorModel)
  {
    this.colorModel = colorModel;
  }

  public void setDimensions (int width,int height)
  {
    this.width = width;
    this.height = height;
  }
  
  public void setHints (int hints)
  {
    this.hints = hints;
  }

  public void setProperties (Hashtable properties)
  {
    this.properties = properties;
  }

  protected Object removeProperty (String key)
  {
    return properties == null ? null : properties.remove (key);
  }

  public void imageComplete (int status)
  {
    if (status == IMAGEERROR)
      throw new Error ("image error");
    if (status == IMAGEABORTED)
      throw new Error ("image aborted");
  }
}
