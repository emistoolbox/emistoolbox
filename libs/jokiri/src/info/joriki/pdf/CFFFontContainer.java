/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

public interface CFFFontContainer
{
  boolean containsCFF ();
  int getIndex (int code);
  byte [] getCFFData ();
}
