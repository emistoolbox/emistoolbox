/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import java.io.DataInput;
import java.io.IOException;

import java.awt.Dimension;

import info.joriki.util.Assertions;
import info.joriki.util.NotTestedException;

class CodingStyle implements JP2Speaker
{
  final static Dimension defaultPrecinctSize = new Dimension (1 << 15,1 << 15);

  int nlevels;
  int waveletTransform;

  boolean verticallyCausalContext;
  boolean arithmeticCodingBypass;
  boolean predictableTermination;
  boolean segmentationSymbols;
  boolean resetProbabilities;
  boolean alwaysTerminate;

  boolean normalTermination;

  Dimension blockSize;
  Dimension [] precinctSizes;

  CodingStyle (DataInput in,int flags) throws IOException
  {
    nlevels = in.readUnsignedByte ();

    int xcb = (in.readUnsignedByte () & 0xf) + 2;
    int ycb = (in.readUnsignedByte () & 0xf) + 2;
    Assertions.limit (xcb,2,10);
    Assertions.limit (ycb,2,10);
    Assertions.expect (xcb + ycb <= 12);
    blockSize = new Dimension (1 << xcb,1 << ycb);

    int blockStyle = in.readUnsignedByte ();
    verticallyCausalContext = (blockStyle & VERTICALLY_CAUSAL_CONTEXT) != 0;
    arithmeticCodingBypass  = (blockStyle & ARITHMETIC_CODING_BYPASS ) != 0;
    predictableTermination  = (blockStyle & PREDICTABLE_TERMINATION  ) != 0;
    segmentationSymbols     = (blockStyle & SEGMENTATION_SYMBOLS     ) != 0;
    resetProbabilities      = (blockStyle & RESET_PROBABILITIES      ) != 0;
    alwaysTerminate         = (blockStyle & ALWAYS_TERMINATE         ) != 0;

    if (resetProbabilities)
      throw new NotTestedException ("reset of probabilities");

    normalTermination = !alwaysTerminate && !arithmeticCodingBypass;

    waveletTransform = in.readUnsignedByte ();

    precinctSizes = new Dimension [nlevels + 1];
    for (int i = 0;i <= nlevels;i++)
      {
	if ((flags & CUSTOM_PRECINCTS) != 0)
	  {
	    int precinctSize = in.readUnsignedByte ();
	    precinctSizes [i] = new Dimension
	      (1 << (precinctSize & 0xf),1 << (precinctSize >> 4));
	    if (nlevels != 0)
	      {
		Assertions.unexpect (precinctSizes [i].width,1);
		Assertions.unexpect (precinctSizes [i].height,1);
	      }
	  }
	else
	  precinctSizes [i] = defaultPrecinctSize;
      }
  }


  final boolean bypass (int pass)
  {
    return arithmeticCodingBypass && pass > 9 && pass % 3 != CLEANUP;
  }

  final boolean terminal (int pass)
  {
    return alwaysTerminate || (bypass (pass) != bypass (pass + 1));
  }

  final boolean initial (int pass)
  {
    return pass == 0 || terminal (pass - 1);
  }
}
