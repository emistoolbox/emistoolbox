/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.font;

public interface DescribedFont extends Font
{
  String getName ();
  String getNotice ();
  String getWeight ();

  GlyphProvider getGlyphProvider (Object glyphSelector);
}
