/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.font;

import info.joriki.graphics.Transformation;

public class TransformedGlyphProvider implements GlyphProvider {
  GlyphProvider provider;
  Transformation transform;
  
  public TransformedGlyphProvider (GlyphProvider provider,Transformation transform) {
    this.provider = provider;
    this.transform = transform;
  }
  
  public void interpret (GlyphInterpreter interpreter) {
    provider.interpret (new TransformedGlyphInterpreter (interpreter,transform));
  }
}
