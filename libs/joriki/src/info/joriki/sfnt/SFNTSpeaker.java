/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

public interface SFNTSpeaker
{
  // file signatures
  int TRUETYPE = 0x00010000; // 1.0
  int OPENTYPE = 0x4f54544f; // "OTTO"

  // table signatures
  String CFF  = "CFF ";
  String CMAP = "cmap";
  String CVT  = "cvt ";
  String FPGM = "fpgm";
  String HEAD = "head";
  String HHEA = "hhea";
  String HMTX = "hmtx";
  String GLYF = "glyf";
  String LOCA = "loca";
  String MAXP = "maxp";
  String NAME = "name";
  String POST = "post";
  String PREP = "prep";
  String OS2  = "OS/2";

  // OpenType table signatures
  String GPOS = "GPOS";
  String GSUB = "GSUB";
}
