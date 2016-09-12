/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.joriki.util.Assertions;

public class OS2Table extends SFNTTable
{
  int length;

  // 0
  int version;
  short xAvgCharWidth;
  int usWeightClass;
  int usWidthClass;
  int fsType;
  short ySubscriptXSize;
  short ySubscriptYSize;
  short ySubscriptXOffset;
  short ySubscriptYOffset;
  short ySuperscriptXSize;
  short ySuperscriptYSize;
  short ySuperscriptXOffset;
  short ySuperscriptYOffset;
  short yStrikeoutSize;
  short yStrikeoutPosition;
  short sFamilyClass;
  byte [] panose = new byte [10];
  byte [] unicodeRange = new byte [16];
  byte [] achVendID = new byte [4];
  int fsSelection;
  int usFirstCharIndex;
  int usLastCharIndex;
  // 68
  short sTypoAscender;
  short sTypoDescender;
  short sTypoLineGap;
  int usWinAscent;
  int usWinDescent;
  // 78
  byte [] codePageRange = new byte [8];
  // 86
  short sxHeight;
  short sCapHeight;
  int usDefaultChar;
  int usBreakChar;
  int usMaxContext;
  // 96
  
  public OS2Table (DataInput in,int length) throws IOException
  {
    super (OS2);
    this.length = length;

    version             = in.readUnsignedShort ();
    xAvgCharWidth       = in.readShort ();
    usWeightClass       = in.readUnsignedShort ();
    usWidthClass        = in.readUnsignedShort ();
    fsType              = in.readUnsignedShort ();
    ySubscriptXSize     = in.readShort ();
    ySubscriptYSize     = in.readShort ();
    ySubscriptXOffset   = in.readShort ();
    ySubscriptYOffset   = in.readShort ();
    ySuperscriptXSize   = in.readShort ();
    ySuperscriptYSize   = in.readShort ();
    ySuperscriptXOffset = in.readShort ();
    ySuperscriptYOffset = in.readShort ();
    yStrikeoutSize      = in.readShort ();
    yStrikeoutPosition  = in.readShort ();
    sFamilyClass        = in.readShort ();
    in.readFully (panose);
    in.readFully (unicodeRange);
    in.readFully (achVendID);
    fsSelection         = in.readUnsignedShort ();
    usFirstCharIndex    = in.readUnsignedShort ();
    usLastCharIndex     = in.readUnsignedShort ();
    if (length == 68)
      return;
    sTypoAscender       = in.readShort ();
    sTypoDescender      = in.readShort ();
    sTypoLineGap        = in.readShort ();
    usWinAscent         = in.readUnsignedShort ();
    usWinDescent        = in.readUnsignedShort ();
    if (length == 78)
      return;
    in.readFully (codePageRange);
    if (length == 86)
      return;
    sxHeight            = in.readShort ();
    sCapHeight          = in.readShort ();
    usDefaultChar       = in.readUnsignedShort ();
    usBreakChar         = in.readUnsignedShort ();
    usMaxContext        = in.readUnsignedShort ();

    Assertions.expect (length,96);
  }

  public void writeTo (DataOutput out) throws IOException
  {
    out.writeShort (version);
    out.writeShort (xAvgCharWidth);
    out.writeShort (usWeightClass);
    out.writeShort (usWidthClass);
    out.writeShort (fsType);
    out.writeShort (ySubscriptXSize);
    out.writeShort (ySubscriptYSize);
    out.writeShort (ySubscriptXOffset);
    out.writeShort (ySubscriptYOffset);
    out.writeShort (ySuperscriptXSize);
    out.writeShort (ySuperscriptYSize);
    out.writeShort (ySuperscriptXOffset);
    out.writeShort (ySuperscriptYOffset);
    out.writeShort (yStrikeoutSize);
    out.writeShort (yStrikeoutPosition);
    out.writeShort (sFamilyClass);
    out.write      (panose);
    out.write      (unicodeRange);
    out.write      (achVendID);
    out.writeShort (fsSelection);
    out.writeShort (usFirstCharIndex);
    out.writeShort (usLastCharIndex);
    if (length == 68)
      return;
    out.writeShort (sTypoAscender);
    out.writeShort (sTypoDescender);
    out.writeShort (sTypoLineGap);
    out.writeShort (usWinAscent);
    out.writeShort (usWinDescent);
    if (length == 78)
      return;
    out.write      (codePageRange);
    if (length == 86)
      return;
    out.writeShort (sxHeight);
    out.writeShort (sCapHeight);
    out.writeShort (usDefaultChar);
    out.writeShort (usBreakChar);
    out.writeShort (usMaxContext);

    Assertions.expect (length,96);
  }

  final static String [] weights = {
    "Thin","Extralight","Light","Regular","Medium",
    "Semibold","Bold","Extrabold","Black"
  };

  public String getWeight ()
  {
    return weights [(usWeightClass - 50) / 100];
  }
}
