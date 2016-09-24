/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

public interface RenderingMode {
  boolean strokes ();
  boolean fills ();
  boolean clips ();
}
