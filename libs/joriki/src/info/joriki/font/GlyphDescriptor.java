/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.font;

import java.util.Arrays;

import info.joriki.util.Options;
import info.joriki.util.Unicode;

public class GlyphDescriptor {
  final static GlyphDescriptor [] ligatures = {
      new GlyphDescriptor ((char) 0xfb00,new char [] {'f','f'}),
      new GlyphDescriptor ((char) 0xfb01,new char [] {'f','i'}),
      new GlyphDescriptor ((char) 0xfb02,new char [] {'f','l'}),
      new GlyphDescriptor ((char) 0xfb03,new char [] {'f','f','i'}),
      new GlyphDescriptor ((char) 0xfb04,new char [] {'f','f','l'}),
      // don't expand from here
      new GlyphDescriptor ((char) 0x00bc,new char [] {'1',0x2044,'4'}),
      new GlyphDescriptor ((char) 0x00bd,new char [] {'1',0x2044,'2'}),
      new GlyphDescriptor ((char) 0x00be,new char [] {'3',0x2044,'4'}),
      // tom findings from 39248
      new GlyphDescriptor ((char) 0x2153,new char [] {'1','/','3'}),
      // by analogy to the above
      new GlyphDescriptor ((char) 0x2154,new char [] {'2','/','3'}),
      new GlyphDescriptor ((char) 0x2155,new char [] {'1','/','5'}),
      new GlyphDescriptor ((char) 0x2156,new char [] {'2','/','5'}),
      new GlyphDescriptor ((char) 0x2157,new char [] {'3','/','5'}),
      new GlyphDescriptor ((char) 0x2158,new char [] {'4','/','5'}),
      new GlyphDescriptor ((char) 0x2159,new char [] {'1','/','6'}),
      new GlyphDescriptor ((char) 0x215a,new char [] {'5','/','6'}),
      new GlyphDescriptor ((char) 0x215b,new char [] {'1','/','8'}),
      new GlyphDescriptor ((char) 0x215c,new char [] {'3','/','8'}),
      new GlyphDescriptor ((char) 0x215d,new char [] {'5','/','8'}),
      new GlyphDescriptor ((char) 0x215e,new char [] {'7','/','8'}),
      // no Unicode from here
      // from EthicsandtheInternet.pdf
      new GlyphDescriptor ((char) 0x0000,new char [] {'T','h'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'b','j'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'r','r'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'f','r'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {0xf4,'t'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'o','t'}),
      // from folio41.pdf
      new GlyphDescriptor ((char) 0x0000,new char [] {'f','j'}),
      // from allured-17.pdf 
      new GlyphDescriptor ((char) 0x0000,new char [] {'c','t'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'s','s'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'s','y'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'e','s','s'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'e','t','t'}),
      // from Phys-94.pdf
      new GlyphDescriptor ((char) 0x0000,new char [] {'f','f','h'}),
      // from cdr200601_46.pdf
      new GlyphDescriptor ((char) 0x0000,new char [] {'e','c'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'e','s'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'e','t'}),
      // without sample file 
      new GlyphDescriptor ((char) 0x0000,new char [] {'t','t'}),
      // from Phys-51.pdf 
      new GlyphDescriptor ((char) 0x0000,new char [] {'f','b'}),
      // from Phys-173.pdf
      new GlyphDescriptor ((char) 0x0000,new char [] {'o','f'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'r','s'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'C','h'}),
      // more tom findings from 33015
      new GlyphDescriptor ((char) 0x0000,new char [] {'f','h'}),
      // yet more tom findings
      new GlyphDescriptor ((char) 0x0000,new char [] {'t','h'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'l','l'}),
      // petra findings from 36290
      new GlyphDescriptor ((char) 0x0000,new char [] {'e','y'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'e','x'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'o','t','t'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'o','f','f'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'i','s','s'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'g','g'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'c','r'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'w','h'}),
      // tom findings from 37166
      new GlyphDescriptor ((char) 0x0000,new char [] {'o','l'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'s','t'}),
      // tom findings from 38244
      new GlyphDescriptor ((char) 0x0000,new char [] {'e','x','t'}),
      // tom findings from 39111
      new GlyphDescriptor ((char) 0x0000,new char [] {'o','o'}),
      // tom findings from DS-147
      new GlyphDescriptor ((char) 0x0000,new char [] {'f','k'}),
      // tom findings from DS-167
      new GlyphDescriptor ((char) 0x0000,new char [] {'t','e'}),
      // tom findings from DS-236
      new GlyphDescriptor ((char) 0x0000,new char [] {'L','A'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'S','S'}),
      new GlyphDescriptor ((char) 0x0000,new char [] {'T','H'}),
      // tom findings from DS-363
      new GlyphDescriptor ((char) 0x0000,new char [] {'r','e'}),
      // tom findings from DS-403
      new GlyphDescriptor ((char) 0x0000,new char [] {'y', '-'}),
      // tom findings from DS-420
      new GlyphDescriptor ((char) 0x0000,new char [] {'f','f','t'}),
      //tom findings from DS-431
      new GlyphDescriptor ((char) 0x0000,new char [] {'O','f'}),
      //tom findings fromm ticket 41584
      new GlyphDescriptor ((char) 0x0000,new char [] {'t','i'})
  };
  final static int nexpand = 5; // expand ligatures up to this index
  
  public int unicode;
  public char [] unicodes;

  public GlyphDescriptor () {}
  
  private GlyphDescriptor(char unicode, char[] unicodes) {
    this.unicode = unicode;
    this.unicodes = unicodes;
  }

  public boolean isTrivial () {
    return
    unicodes == null ||
    (unicodes.length == 1 && unicode == unicodes [0]) ||
    (unicodes.length == 2 && unicode == Unicode.toCodePoint (unicodes [0],unicodes [1]));
  }

  public void fill () {
    if (unicodes == null && unicode != 0)
    {  
      for (int i = 0;i < nexpand;i++)
        if (unicode == ligatures [i].unicode)
        {
          unicodes = ligatures [i].unicodes;
          break;
        }
    }
    else if (unicode == 0 && unicodes != null)
    {
      if (unicodes.length == 1)
        unicode = unicodes [0];
      else if (unicodes.length == 2 && Unicode.isSurrogatePair (unicodes [0],unicodes [1]))
        unicode = Unicode.toCodePoint (unicodes [0],unicodes [1]);
      else
      {
        for (int i = 0;i < ligatures.length;i++)
          if (Arrays.equals (unicodes,ligatures [i].unicodes))
          {
            unicode = ligatures [i].unicode;
            return;
          }
        Options.warn ("unknown ligature " + new String (unicodes));
      }
    }
  }
  
  public boolean equals (Object o) {
    return o instanceof GlyphDescriptor ? unicode == ((GlyphDescriptor) o).unicode : false;
  }
  
  public int hashCode () {
    return unicode;
  }

  public String toString () {
    StringBuilder stringBuilder = new StringBuilder ();
    stringBuilder.append ("0x").append (Integer.toHexString (unicode));
    if (unicodes != null)
      stringBuilder.append (" (").append (unicodes).append (')');
    return stringBuilder.toString ();
  }
}
