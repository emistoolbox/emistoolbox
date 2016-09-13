/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import java.awt.image.ColorModel;

import java.util.Hashtable;

import info.joriki.io.filter.SourceRecipient;

public abstract class ImageDecoder extends AbstractImageProducer implements SourceRecipient
{
  protected int width;
  protected int height;

  protected int hints;
  protected ColorModel colorModel;
  protected Hashtable properties;

  public void setPixels (int y,Object row)
  {
    setPixels (y,1,row);
  }
  
  public void setPixels (int y,int h,Object row)
  {
    setPixels (y,h,row,width);
  }

  public void setPixels (int y,int h,Object row,int scanLength)
  {
    setPixels (0,y,width,h,row,scanLength);
  }

  public void setPixels (int y,int h,Object row,int offset,int scanLength) {
    setPixels (0,y,width,h,row,offset,scanLength);
  }
  
  public void setPixels (int x,int y,int w,int h,Object row,int scanLength) {
    setPixels (x,y,w,h,row,0,scanLength);
  }
  
  public void setPixels (int x,int y,int w,int h,Object row,int offset,int scanLength)
  {
    for (int i = 0;i < consumers.length;i++)
      if (row instanceof byte [])
        consumers [i].setPixels (x,y,w,h,colorModel,(byte []) row,offset,scanLength);
      else
        consumers [i].setPixels (x,y,w,h,colorModel,(int []) row,offset,scanLength);
  }

  protected void setProperty (Object key,Object value)
  {
    if (properties == null)
      properties = new Hashtable ();
    properties.put (key,value);
  }

  protected void setParameters ()
  {
    setParameters (width,height,colorModel,hints,properties);
  }
}
