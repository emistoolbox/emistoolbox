/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import info.joriki.io.filter.Crank;

public abstract class JPEGScribe extends JPEGTerminal implements Crank {
  abstract void setICCData (byte [] iccData);
}
