/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.awt.image.ImageOptions;

import info.joriki.awt.image.jpeg.JPEGOptions;

import info.joriki.xml.XMLOptions;

import info.joriki.util.Switch;

import info.joriki.truetype.TrueTypeOptions;

public interface PDFOptions extends ImageOptions, JPEGOptions, TrueTypeOptions, XMLOptions
{
  Switch approximateColors    = new Switch ("don't worry too much about precise color transforms");
  Switch cacheColors          = new Switch ("cache transformed color values");
  Switch adobeCMYK            = new Switch ("make CMYK colors appear as in Acrobat");
  Switch oldAdobeCMYK         = new Switch ("make CMYK colors appear as they used to in Acrobat");
  Switch omitStandardFonts    = new Switch ("don't include standard fonts in output");
  Switch synthesizeFonts      = new Switch ("synthesize non-embedded fonts from multiple masters");
  Switch restoreGlyphs        = new Switch ("restore glyphs from invalid TrueType fonts");
  Switch ignoreChecksums      = new Switch ("ignore incorrect checksums in ZLIB streams");
  Switch scrambleUnicodes     = new Switch ("scramble Unicodes");
  Switch loadIntoMemory       = new Switch ("load entire PDF file into memory (slightly faster)");
  Switch ignoreUnknownEntries = new Switch ("ignore unknown dictionary entries");
}
