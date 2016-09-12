/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import java.awt.image.ImageConsumer;

import info.joriki.util.Switch;

public interface ImageProperties
{
  int SIMPLE_HINTS =
    ImageConsumer.TOPDOWNLEFTRIGHT |
    ImageConsumer.COMPLETESCANLINES |
    ImageConsumer.SINGLEFRAME |
    ImageConsumer.SINGLEPASS;
  String LAST_MODIFICATION_TIME = "LastModificationTime";
  String BITS_PER_COMPONENT = "BitsPerComponent";
  String BACKGROUND_COLOR = "BackgroundColor";
  String PIXEL_DIMENSIONS = "PixelDimensions";
  String ICC_PROFILE_NAME = "ICCProfileName";
  String COMMENT = "Comment";

  public final Switch standardColorModels = new Switch ("use only standard RGB and gray color models");
  public final Switch noGammaChunk = new Switch ("don't write a gamma value into image files");
  public final Switch noSRGBChunk = new Switch ("don't write an sRGB chunk into PNG files");
}
