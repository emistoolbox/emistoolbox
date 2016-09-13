/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.crypto.ArcFourCypher;
import info.joriki.crypto.MD5;
import info.joriki.crypto.StreamCypher;

public class Crypt extends MD5 {
  int object;
  int generation;
  
  byte [] cryptKey;

  public Crypt (byte [] cryptKey) {
    this.cryptKey = cryptKey;
  }
  
  public StreamCypher getCypher () {
    if (cryptKey == null)
      return null;
    reset ();
    digest (cryptKey);
    digest (object,3);
    digest (generation,2);
    byte [] key = getDigest ();
    return new ArcFourCypher (key,0,Math.min (key.length,cryptKey.length + 5));
  }
  
  void decrypt (PDFString string)
  {
    if (cryptKey != null) {
      StreamCypher cypher = getCypher ();
      byte [] str = string.str;
      for (int i = 0;i < str.length;i++)
        str [i] = cypher.decrypt (str [i]);
    }
  }
  
  void encrypt (PDFString string)
  {
    if (cryptKey != null) {
      StreamCypher cypher = getCypher ();
      byte [] str = string.str;
      for (int i = 0;i < str.length;i++)
        str [i] = cypher.encrypt (str [i]);
    }
  }
}
