/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.font;

import info.joriki.truetype.OutlineInterpreter;

import info.joriki.charstring.CharStringInterpreter;

public interface GlyphInterpreter
  extends CharStringInterpreter, OutlineInterpreter {}
