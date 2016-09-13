/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.adobe;

import java.io.IOException;
import java.io.InputStream;

import info.joriki.util.General;
import info.joriki.util.Options;

public class ToUnicodeMap extends CMapFile
{
  int lengthIndex;
  
  public ToUnicodeMap (InputStream in,int codeLength) throws IOException
  {
    super (in);
    lengthIndex = codeLength - 1;
  }

  public char [] getUnicodes (int code)
  {
    byte [] string = (byte []) singleCodes [lengthIndex].get (new Integer (code));
    if (string != null)
      return General.packIntoChars (string);
    char unicode = rangeMap (code,lengthIndex);
    return unicode == 0 ? null : new char [] {unicode};
  }

  protected void parse () throws IOException {
    try {
      super.parse ();
      return;
    } catch (RuntimeException re) {
      if (cmapDictionary == null)
        throw re;
    } catch (IOException ioe) {
      if (cmapDictionary == null)
        throw ioe;
    }
    // f182006_spring_33.pdf
    Options.warn ("trailing garbage after ToUnicode map");
  }
  
}
