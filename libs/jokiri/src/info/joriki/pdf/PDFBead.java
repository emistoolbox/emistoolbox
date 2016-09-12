/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.Assertions;
import info.joriki.graphics.Rectangle;

public class PDFBead
{
  private final static String NEXT = "N";
  private final static String PREV = "V";
  private final static String RECT = "R";
  private final static String PAGE = "P";
  
  public PDFDictionary pageObject;
  public Rectangle rectangle;
  public int threadNumber;
  public int beadNumber;

  public PDFBead (PDFDictionary dictionary,
                  int threadNumber,
                  int beadNumber)
  {
    this.threadNumber = threadNumber;
    this.beadNumber = beadNumber;

    Assertions.expect (dictionary.isOptionallyOfType("Bead"));
    
    rectangle = dictionary.getRectangle (RECT);
    pageObject = (PDFDictionary) dictionary.get (PAGE);
    
    Assertions.expect (((PDFDictionary) dictionary.get (NEXT)).get (PREV),dictionary);
    Assertions.expect (((PDFDictionary) dictionary.get (PREV)).get (NEXT),dictionary);

    dictionary.use ("T"); // optional except for first bead : containing thread
    
    dictionary.checkUnused ("8.8");
  }
}
