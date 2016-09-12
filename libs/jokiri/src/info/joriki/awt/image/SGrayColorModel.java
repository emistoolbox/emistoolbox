/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

public class SGrayColorModel extends GrayColorModel
{
  public final static SGrayColorModel sGRAY = new SGrayColorModel (false);

  private SGrayColorModel (boolean hasAlpha)
  {
    super (hasAlpha);
  }
}
