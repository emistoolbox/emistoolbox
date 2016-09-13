/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.truetype;

import java.io.PrintStream;

import info.joriki.util.General;

public class ByteCodeSpeaker
{
  final static String [] mnemonic = {
    "SVTCA[0]",
    "SVTCA[1]",
    "SPVTCA[0]",
    "SPVTCA[1]",
    "SFVTCA[0]",
    "SFVTCA[1]",
    "SPVTL[0]",
    "SPVTL[1]",
    "SFVTL[0]",
    "SFVTL[1]",
    "SPVFS",
    "SFVFS",
    "GPV",
    "GFV",
    "SFVTPV",
    "ISECT",
    // 0x10
    "SRP0",
    "SRP1",
    "SRP2",
    "SZP0",
    "SZP1",
    "SZP2",
    "SZPS",
    "SLOOP",
    "RTG",
    "RTHG",
    "SMD",
    "ELSE",
    "JMPR",
    "SCVTCI",
    "SSWCI",
    "SSW",
    // 0x20
    "DUP",
    "POP",
    "CLEAR",
    "SWAP",
    "DEPTH",
    "CINDEX",
    "MINDEX",
    "ALIGNPTS",
    null,
    "UTP",
    "LOOPCALL",
    "CALL",
    "FDEF",
    "ENDF",
    "MDAP[0]",
    "MDAP[1]",
    // 0x30
    "IUP[0]",
    "IUP[1]",
    "SHP[0]",
    "SHP[1]",
    "SHC[0]",
    "SHC[1]",
    "SHZ[0]",
    "SHZ[1]",
    "SHPIX",
    "IP",
    "MSIRP[0]",
    "MSIRP[1]",
    "ALIGNRP",
    "RTDG",
    "MIAP[0]",
    "MIAP[1]",
    // 0x40
    "NPUSHB",
    "NPUSHW",
    "WS",
    "RS",
    "WCVTP",
    "RCVT",
    "GC[0]",
    "GC[1]",
    "SCFS",
    "MD[0]",
    "MD[1]",
    "MPPEM",
    "MPS",
    "FLIPON",
    "FLIPOFF",
    "DEBUG",
    // 0x50
    "LT",
    "LTEQ",
    "GT",
    "GTEQ",
    "EQ",
    "NEQ",
    "ODD",
    "EVEN",
    "IF",
    "EIF",
    "AND",
    "OR",
    "NOT",
    "DELTAP1",
    "SDB",
    "SDS",
    // 0x60
    "ADD",
    "SUB",
    "DIV",
    "MUL",
    "ABS",
    "NEG",
    "FLOOR",
    "CEILIING",
    "ROUND",
    "ROUND",
    "ROUND",
    "ROUND",
    "NROUND",
    "NROUND",
    "NROUND",
    "NROUND",
    // 0x70
    "WCVTF",
    "DELTAP2",
    "DELTAP3",
    "DELTAC1",
    "DELTAC2",
    "DELTAC3",
    "SROUND",
    "S45ROUND",
    "JROT",
    "JROF",
    "ROFF",
    null,
    "RUTG",
    "RDTG",
    "SANGW",
    "AA",
    // 0x80
    "FLIPPT",
    "FLIPRGON",
    "FLIPRGOFF",
    null,
    null,
    "SCANCTRL",
    "SDPVTL[0]",
    "SDPVTL[1]",
    "GETINFO",
    "IDEF",
    "ROLL",
    "MAX",
    "MIN",
    "SCANTYPE",
    "INSTCTRL",
    null
  };

  final static int [] npop = {
    0,0,0,0,0,0,2,2,2,2,2,2,0,0,0,5,
    1,1,1,1,1,1,1,1,0,0,1,0,1,1,1,1,
    1,1,0,2,0,0,0,2,0,1,1,1,1,0,1,1,
    0,0,0,0,1,1,1,1,1,0,2,2,0,0,2,2,
    0,0,2,1,2,1,1,1,2,2,2,0,0,0,0,1,
    2,2,2,2,2,2,1,1,1,0,2,2,1,0,1,1,
    2,2,2,2,1,1,1,1,1,1,1,1,1,1,1,1,
    2,0,0,0,0,0,1,1,2,2,0,0,0,0,1,1,
    0,2,2,0,0,1,2,2,1,1,0,2,2,1,2
  };
  final static int [] npush = {
    0,0,0,0,0,0,0,0,0,0,0,0,2,2,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    2,0,0,2,1,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,1,0,1,1,1,0,1,1,1,1,0,0,0,
    1,1,1,1,1,1,1,1,0,0,1,1,1,0,0,0,
    1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,1,0,0,1,1,0,0
  };

  int nest = 0;
  PrintStream log;

  public ByteCodeSpeaker (PrintStream log)
  {
    this.log = log;
  }

  void write (String mnemonic)
  {
    for (int i = 0;i < nest;i++)
      log.print ("  ");
    log.print (mnemonic);
  }

  void write (String mnemonic,int opcode,int nflags)
  {
    write (mnemonic);
    log.print
      ("[" + General.zeroPad
       (Integer.toBinaryString
        (opcode & ((1 << nflags) - 1)),nflags) + "]");
  }

  void log (int opcode)
  {
    if ((opcode & 0xf8) == 0xb0) // PUSHB[abc]
      write ("PUSHB",opcode,3);
    else if ((opcode & 0xf8) == 0xb8) // PUSHW[abc]
      write ("PUSHW",opcode,3);
    else if ((opcode & 0xe0) == 0xc0) // MDRP[abcde]
      write ("MDRP",opcode,5);
    else if ((opcode & 0xe0) == 0xe0) // MIRP[abcde]
      write ("MIRP",opcode,5);
    else
      {
        if (opcode == 0x1b || opcode == 0x59) // ELSE or EIF
          nest--;
        write (mnemonic [opcode]);
        if (opcode == 0x1b || opcode == 0x58) // ELSE or IF
          nest++;
      }
  }
}
