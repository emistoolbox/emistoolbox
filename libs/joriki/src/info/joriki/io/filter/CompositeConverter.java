/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

/**
   A composite converter is not itself crankable,
   but it knows which of its components needs to
   be cranked to achieve the effect of cranking
   the converter as a whole.
*/

public interface CompositeConverter extends Converter
{
  /**
     Returns the crank that cranks this converter.
     @return the crank that cranks this converter
  */
  public Crank getCrank ();
}
