/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import java.util.HashMap;

import info.joriki.awt.image.CartesianTransform;

public class JPEGFormat extends HashMap
{
  MCULayout layout;
  int width,height;
  int pixelWidth,pixelHeight;
  int lastWidth,lastHeight;
  MCU [] [] mcus;

  private JPEGFormat () {}

  JPEGFormat (MCULayout layout,int pixelWidth,int pixelHeight)
  {
    this (layout,
          (pixelWidth - 1) / layout.width + 1,
          (pixelHeight - 1) / layout.height + 1,
          pixelWidth,pixelHeight);
  }

  JPEGFormat (MCULayout layout,int width,int height,int pixelWidth,int pixelHeight)
  {
    this.layout = layout;
    this.width = width;
    this.height = height;
    this.pixelWidth = pixelWidth;
    this.pixelHeight = pixelHeight;
    this.mcus = new MCU [height] [width];
    this.lastWidth = pixelWidth - (width - 1) * layout.width;
    this.lastHeight = pixelHeight - (height - 1) * layout.height;
  }

  JPEGFormat (JPEGFormat format)
  {
    this (format.layout,format);
    putAll (format);
  }

  JPEGFormat (MCULayout layout,JPEGFormat format)
  {
    this (layout,format.width,format.height,format.pixelWidth,format.pixelHeight);
    putAll (format);
  }

  JPEGFormat transformedBy (CartesianTransform transform)
  {
    MCULayout transformedLayout = layout.transformedBy (transform);
    return transform.swaps () ?
      new JPEGFormat (transformedLayout,height,width,pixelHeight,pixelWidth) :
      new JPEGFormat (transformedLayout,width,height,pixelWidth,pixelHeight);
  }
}
