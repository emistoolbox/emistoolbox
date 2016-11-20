/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import java.awt.Dimension;

import info.joriki.util.General;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

public class TileComponent extends Dimension implements JP2Speaker
{
  // Table F-7
  final static double A =  Math.pow ((63 / Math.sqrt (15) - 14) / 1080,1./3);
  final static double B = -Math.pow ((63 / Math.sqrt (15) + 14) / 1080,1./3);
  final static double x1 = A + B - 1./6;
  final static double x2re = -(A + B) / 2 - 1./6;
  final static double x2sq = x2re * x2re + 3 * (A - B) * (A - B) / 4;

  // Table F-5
  final static double g0 =  5 * x1 * (48 * x2sq - 16 * x2re + 3) / 32;
  final static double g1 = -5 * x1 * ( 8 * x2sq -      x2re    ) /  8;
  final static double g2 =  5 * x1 * ( 4 * x2sq +  4 * x2re - 1) / 16;
  final static double g3 = -5 * x1 *                   x2re      /  8;
  final static double g4 =  5 * x1                               / 64;

  // Table F-6
  final static double r0 = -g0 +  2 * g1 * g4 / g3;
  final static double r1 = -g2 + g4 + g1 * g4 / g3;
  final static double s0 =  g1 - g3 - g3 * r0 / r1;
  final static double t0 =  r0 -  2 * r1;

  // Table F-4
  final static float alpha = (float) (-g4/g3);
  final static float beta  = (float)  (g3/r1);
  final static float gamma = (float)  (r1/s0);
  final static float delta = (float)  (s0/t0);
  final static float K     = (float)   (1/t0);

  final static float [] lifts = {alpha,beta,gamma,delta};

  byte [] data;

  int [] intBuf;
  int [] intTmp;

  float [] floatBuf;
  float [] floatTmp;

  BitDescriptor bitDescriptor;
  CodingStyle codingStyle;
  QuantizationStyle quantizationStyle;
  ResolutionLevel [] resolutionLevels;
  
  TileComponent (int width,int height,BitDescriptor bitDescriptor,CodingStyle codingStyle,QuantizationStyle quantizationStyle)
  {
    super (width,height);
    this.bitDescriptor = bitDescriptor;
    this.codingStyle = codingStyle;
    this.quantizationStyle = quantizationStyle;

    Assertions.expect
      (codingStyle.waveletTransform == REVERSIBLE_5_3,
       quantizationStyle.quantizationStyle == NO_QUANTIZATION);

    int size = width * height;
    int tmpSize = Math.max (width,height);

    data = new byte [size];

    switch (codingStyle.waveletTransform)
      {
      case IRREVERSIBLE_9_7 :
	floatBuf = new float [size];
	floatTmp = new float [tmpSize];
	break;
      case REVERSIBLE_5_3 :
	intBuf = new int [size];
	intTmp = new int [tmpSize];
	break;
      default :
	throw new NotImplementedException ("wavelet transform " + codingStyle.waveletTransform);
      }

    int level = codingStyle.nlevels;
    resolutionLevels = new ResolutionLevel [level + 1];
    resolutionLevels [level] = new ResolutionLevel (this,codingStyle,level);
    while (level-- > 0)
      resolutionLevels [level] = resolutionLevels [level + 1].nextLevel;
  }

  // I believe all this depends on the fact that we start at (0,0).
  // if we start on an odd boundary, we need to make some changes,
  // but I suspect we can keep the general structure
  // transforms of length one on odd boundaries pick up a factor of 2

  void transform (int length,int count,int scan,int step)
  {
    int k;
    if (length > 1)
      for (int i = 0,index = 0;i < count;i++,index += scan)
	switch (codingStyle.waveletTransform)
	  {
	  case IRREVERSIBLE_9_7 :
	    for (int in = 0,out = index;in < length;in++,out += step)
	      floatTmp [in] = floatBuf [out];

	      for (int j = 0;j < lifts.length;j++)
		lift (j,length,1);

	    for (int parity = 0,in = index;parity < 2;parity++)
	      {
		float scale = parity == 0 ? 1 / K : K;
		for (int out = parity;out < length;out += 2,in += step)
		  floatBuf [in] = scale * floatTmp [out];
	      }
	    break;
	  case REVERSIBLE_5_3 :
	    for (int in = 0,out = index;in < length;in++,out += step)
	      intTmp [in] = intBuf [out];

	    for (k = 1;k < length - 1;k += 2)
	      intTmp [k] -= (intTmp [k - 1] + intTmp [k + 1]) >> 1;
	    if ((length & 1) == 0) // right boundary
	      intTmp [k] -= intTmp [k - 1];

	    intTmp [0] += (intTmp [1] + 1) >> 1; // left boundary
	    for (k = 2;k < length - 1;k += 2)
	      intTmp [k] += (intTmp [k - 1] + intTmp [k + 1] + 2) >> 2;
	    if ((length & 1) == 1) // right boundary
	      intTmp [k] += (intTmp [k - 1] + 1) >> 1;

	    for (int parity = 0,in = index;parity < 2;parity++)
	      for (int out = parity;out < length;out += 2,in += step)
		intBuf [in] = intTmp [out];
	    break;
	  default :
	    throw new NotImplementedException ("wavelet transform " + codingStyle.waveletTransform);
	  }
  }

  void inverseTransform (int length,int count,int scan,int step)
  {
    int k;
    if (length > 1)
      for (int i = 0,index = 0;i < count;i++,index += scan)
	switch (codingStyle.waveletTransform)
	  {
	  case IRREVERSIBLE_9_7 :
	    for (int parity = 0,in = index;parity < 2;parity++)
	      {
		float scale = parity == 0 ? K : 1 / K;
		for (int out = parity;out < length;out += 2,in += step)
		  floatTmp [out] = scale * floatBuf [in];
	      }

	    for (int j = lifts.length - 1;j >= 0;j--)
	      lift (j,length,-1);

	    for (int in = 0,out = index;in < length;in++,out += step)
	      floatBuf [out] = floatTmp [in];
	    break;
	  case REVERSIBLE_5_3 :
	    for (int parity = 0,in = index;parity < 2;parity++)
	      for (int out = parity;out < length;out += 2,in += step)
		intTmp [out] = intBuf [in];
	    
	    intTmp [0] -= (intTmp [1] + 1) >> 1; // left boundary
	    for (k = 2;k < length - 1;k += 2)
	      intTmp [k] -= (intTmp [k - 1] + intTmp [k + 1] + 2) >> 2;
	    if ((length & 1) == 1) // right boundary
	      intTmp [k] -= (intTmp [k - 1] + 1) >> 1;

	    for (k = 1;k < length - 1;k += 2)
	      intTmp [k] += (intTmp [k - 1] + intTmp [k + 1]) >> 1;
	    if ((length & 1) == 0) // right boundary
	      intTmp [k] += intTmp [k - 1];

	    for (int in = 0,out = index;in < length;in++,out += step)
	      intBuf [out] = intTmp [in];
	    break;
	  default :
	    throw new NotImplementedException ("wavelet transform " + codingStyle.waveletTransform);
	  }
  }

  void lift (int index,int length,int sign)
  {
    float lift = sign * lifts [index];
    float doubleLift = 2 * lift;
    int parity = index & 1;
    int limit = length - 1;
    int k;

    if (parity == 1) // left boundary
      floatTmp [0] += doubleLift * floatTmp [1];
    for (k = 1 + parity;k < limit;k += 2)
      floatTmp [k] += lift * (floatTmp [k - 1] + floatTmp [k + 1]);
    if (parity == (length & 1)) // right boundary
      floatTmp [k] += doubleLift * floatTmp [k - 1];
  }

  void perform (int action)
  {
    boolean floats = floatBuf != null;
    switch (action)
      {
      case SHIFT :
	if (bitDescriptor.signed)
	  return;
	int shift = 1 << (bitDescriptor.depth - 1);
	if (floats)
	  for (int i = 0;i < floatBuf.length;i++)
	    floatBuf [i] += shift;
	else
	  for (int i = 0;i < intBuf.length;i++)
	    intBuf [i] += shift;
	break;
      case TRANSFORM :
	for (int level = codingStyle.nlevels;level > 0;level--)
	  {
	    ResolutionLevel resolutionLevel = resolutionLevels [level];
	    transform (resolutionLevel.height,resolutionLevel.width,1,width); // columns
	    transform (resolutionLevel.width,resolutionLevel.height,width,1); // rows
	  }
	break;
      case INVERSETRANSFORM :
	for (int level = 1;level <= codingStyle.nlevels;level++)
	  {
	    ResolutionLevel resolutionLevel = resolutionLevels [level];
	    inverseTransform (resolutionLevel.width,resolutionLevel.height,width,1); // rows
	    inverseTransform (resolutionLevel.height,resolutionLevel.width,1,width); // columns
	  }
	break;
      case TOBYTES :
	for (int i = 0;i < data.length;i++)
	  data [i] = (byte) General.clip
	    (floats ? (int) (floatBuf [i] + .5f) : intBuf [i],0,255);
	break;
      default :
	throw new InternalError ("invalid action " + action);
      }
  }
}
