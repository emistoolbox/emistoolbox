/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.png;

public interface PNGSpeaker
{
  byte [] magic = {(byte) 137,80,78,71,13,10,26,10};

  int [] samplesPerType = {1,0,3,1,2,0,4};

  int headerLength = 13;

  // color type flags, see 4.1.1 in the spec
  byte PALETTE = 1;
  byte COLOR   = 2;
  byte ALPHA   = 4;

  // color types
  byte GRAYSCALE       = 0;
  byte TRUECOLOR       = COLOR;
  byte PALETTECOLOR    = COLOR | PALETTE;
  byte GRAYSCALE_ALPHA = ALPHA;
  byte TRUECOLOR_ALPHA = COLOR | ALPHA;

  // special color types for PDF
  byte SAMPLES = -2;
  byte BYTES   = -1;

  // filter algorithms, see 6.1 in the spec
  byte VAR     = -1; // variable, encoded before each row
  byte NONE    = 0;
  byte SUB     = 1;
  byte UP      = 2;
  byte AVERAGE = 3;
  byte PAETH   = 4;
  byte NFILTER = 5;

  // chunk types, see 4 in the spec
  int IHDR = 0x49484452;
  int PLTE = 0x504c5445;
  int IDAT = 0x49444154;
  int IEND = 0x49454e44;
  int cHRM = 0x6348524d;
  int gAMA = 0x67414d41;
  int sBIT = 0x73424954;
  int bKGD = 0x624b4744;
  int hIST = 0x68495354;
  int tRNS = 0x74524e53;
  int pHYs = 0x70485973;
  int tIME = 0x74494d45;
  int tEXt = 0x74455874;
  int zTXt = 0x7a545874;
  int sRGB = 0x73524742;
  int iCCP = 0x69434350;
}
