/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

abstract class JPEGResampler extends JPEGCrank
{
  JPEGRequest readRequest = new JPEGRequest (DIRECT);
  JPEGRequest writeRequest = new JPEGRequest (DIRECT);

  int hdim,vdim;
  int hfac,vfac;

  int x,y;

  public boolean isTrivial ()
  {
    return inputFormat.layout.equals (outputFormat.layout);
  }

  protected void setFactors (MCULayout smallLayout,MCULayout bigLayout)
  {
    hdim = (hfac = bigLayout.blockWidth / smallLayout.blockWidth) << 3;
    vdim = (vfac = bigLayout.blockHeight / smallLayout.blockHeight) << 3;
  }
}
