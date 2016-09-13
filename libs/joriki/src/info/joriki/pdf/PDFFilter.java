/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

public class PDFFilter {
  String name;
  PDFDictionary decodeParameters;
  
  public PDFFilter (String name,PDFDictionary decodeParameters) {
    this.name = name;
    this.decodeParameters = decodeParameters;
  }
}
