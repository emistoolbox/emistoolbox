/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import java.awt.image.ColorModel;

public class UltimateImageConsumer extends StaticImageConsumer
{
  public void setPixels (int x,int y,int w,int h,ColorModel colorModel,byte [] pixels,int off,int scan) {}
  public void setPixels (int x,int y,int w,int h,ColorModel colorModel,int  [] pixels,int off,int scan) {}
}
