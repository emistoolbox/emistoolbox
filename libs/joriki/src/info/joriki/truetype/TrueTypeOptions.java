/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.truetype;

import info.joriki.util.Switch;

public interface TrueTypeOptions {
  Switch enableHinting = new Switch ("enable TrueType font hinting");
}
