/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import java.io.DataInput;
import java.io.IOException;

import info.joriki.util.Assertions;
import info.joriki.util.NotTestedException;
import info.joriki.util.NotImplementedException;

class QuantizationStyle implements JP2Speaker
{
  final static int mantissaBits = 11;
  final static int mantissaMask = 1 << mantissaBits;

  float [] [] [] mantissa;
  int [] [] [] exponent;
  int quantizationStyle;
  int guardBits;

  QuantizationStyle (DataInput in,int length) throws IOException
  {
    quantizationStyle = in.readUnsignedByte ();
    guardBits = quantizationStyle >> 5;
    quantizationStyle &= 0x1f;

    int nlevels;
    switch (quantizationStyle)
      {
      case NO_QUANTIZATION :
	nlevels = length / 3;
	Assertions.expect (length,3 * nlevels + 1);
	break;
      case SCALAR_DERIVED :
	Assertions.expect (length,2);
	throw new NotImplementedException ("scalar derived quantization");
      case SCALAR_EXPOUNDED :
	nlevels = length / 6;
	Assertions.expect (length,(3 * nlevels + 1) << 1);
	break;
      default :
	throw new NotImplementedException ("quantization style " + quantizationStyle);
      }

    if (quantizationStyle != NO_QUANTIZATION)
      mantissa = new float [nlevels + 1] [2] [2];
    exponent = new int [nlevels + 1] [2] [2];

    for (int level = 0;level <= nlevels;level++)
      for (int suby = 0;suby < 2;suby++)
	for (int subx = 0;subx < 2;subx++)
	  if ((subx == 0 && suby == 0) == (level == 0))
	    switch (quantizationStyle)
	      {
	      case NO_QUANTIZATION :
		exponent [level] [subx] [suby] = in.readUnsignedByte () >> 3;
		break;
	      case SCALAR_DERIVED :
		if (level != 0)
		  {
		    mantissa [level] [subx] [suby] = mantissa [0] [0] [0];
		    exponent [level] [subx] [suby] = exponent [0] [0] [0] + level - 1;
		    break;
		  }
		throw new NotTestedException ("scalar derived quantization");
		// fall through for level == 0
	      case SCALAR_EXPOUNDED :
		int stepSize = in.readUnsignedShort ();
		mantissa [level] [subx] [suby] = 1 + (stepSize & (mantissaMask - 1)) / (float) mantissaMask;
		exponent [level] [subx] [suby] = stepSize >> mantissaBits;
		break;
	      default :
		throw new NotImplementedException ("quantization style " + quantizationStyle);
	      }
  }
}
