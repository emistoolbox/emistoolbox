/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.xml;

import info.joriki.util.Switch;

public interface XMLOptions {
  Switch bareXML = new Switch ("don't insert whitespace to display XML structure");
}
