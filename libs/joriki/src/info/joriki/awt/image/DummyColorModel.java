/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

public class DummyColorModel extends EfficientColorModel {
  public DummyColorModel (int ncomponents,boolean hasAlpha) {
    super (ncomponents,hasAlpha);
  }

  protected int getOpaqueRGB (int pixel) {
    throw new InternalError ();
  }

  public boolean equals (Object o) {
    throw new InternalError ();
  }
}
