/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

public interface JP2Speaker
{
  // box types
  int jP   = 0x6a502020;
  int jp2  = 0x6a703220;
  int jp2c = 0x6a703263;
  int jp2h = 0x6a703268;
  int ftyp = 0x66747970;
  int rreq = 0x72726571;
  int ihdr = 0x69686472;
  int colr = 0x636f6c72;
  int xml  = 0x786d6c20;
  int pclr = 0x70636c72;
  int cmap = 0x636d6170;

  // markers
  int SOC = 0x4f;
  int SIZ = 0x51;
  int COD = 0x52;
  int COC = 0x53;
  int QCD = 0x5c;
  int QCC = 0x5d;
  int RGN = 0x5e;
  int COM = 0x64;
  int SOT = 0x90;
  int SOP = 0x91;
  int EPH = 0x92;
  int SOD = 0x93;
  int EOC = 0xd9;

  // enumerated color spaces
  int CMYK  = 12;
  int sRGB  = 16;
  int sGRAY = 17;

  // coding style flags
  int CUSTOM_PRECINCTS = 1 << 0;
  int SOP_MARKERS      = 1 << 1;
  int EPH_MARKERS      = 1 << 2;

  // progression orders
  int RESOLUTION = 0;
  int COMPONENT  = 1;
  int PRECINCT   = 2;
  int LAYER      = 3;

  // code block styles
  int ARITHMETIC_CODING_BYPASS  = 1 << 0;
  int RESET_PROBABILITIES       = 1 << 1;
  int ALWAYS_TERMINATE          = 1 << 2;
  int VERTICALLY_CAUSAL_CONTEXT = 1 << 3;
  int PREDICTABLE_TERMINATION   = 1 << 4;
  int SEGMENTATION_SYMBOLS      = 1 << 5;

  // wavelet transforms
  int IRREVERSIBLE_9_7 = 0;
  int REVERSIBLE_5_3   = 1;

  // quantization styles
  int NO_QUANTIZATION  = 0;
  int SCALAR_DERIVED   = 1;
  int SCALAR_EXPOUNDED = 2;

  // coding pass types
  int CLEANUP      = 0;
  int SIGNIFICANCE = 1;
  int MAGNITUDE    = 2;

  // context types
  int SIGN         = 0;
  //  SIGNIFICANCE = 1;
  //  MAGNITUDE    = 2;

  /* context state : xdsr faaa aacc cccc
     x : unused
     d : done
     s : sign
     r : refined
     f : significant
     a : sign context + 12
     c : significance context */
  int DONE        = 1 << 15;
  int SIGNFLAG    = 1 << 14;
  int REFINED     = 1 << 13;
  int SIGNIFICANT = 1 << 12;
  int SIGNMASK    = 0x7c0;
  int SIGNSHIFT   = 6;
  int SIGNOFFSET  = 12;
  int CONTEXT     = 0x3f;

  // tile component actions
  int SHIFT             = 0;
  int TOBYTES           = 1;
  int TRANSFORM         = 2;
  int INVERSETRANSFORM  = 3;
}
