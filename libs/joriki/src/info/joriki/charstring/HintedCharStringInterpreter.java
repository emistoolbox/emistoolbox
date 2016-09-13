/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.charstring;

public interface HintedCharStringInterpreter extends CharStringInterpreter
{
  void hstem (double y,double dy);
  void vstem (double x,double dx);
  void changeHints ();
}
