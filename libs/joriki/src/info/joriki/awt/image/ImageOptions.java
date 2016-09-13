/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import info.joriki.util.StringParameter;
import info.joriki.util.Switch;

public interface ImageOptions {
  Switch noGrayscaleImages = new Switch ("transform grayscale images to RGB");
  StringParameter CMYKprofile = new StringParameter (null,"use ICC profile $ to convert from CMYK to RGB");
}
