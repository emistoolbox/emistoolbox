/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.Options;

public class PDFDestination
{
  public PDFArray array;

  public PDFDestination (PDFArray array)
  {
    this.array = array;
  }

  public int getPageNumber ()
  {
    PDFObject pageReference = array.get (0);
    if (pageReference instanceof PDFInteger)
      return ((PDFInteger) pageReference).val + 1;
    if (pageReference instanceof PDFDictionary)
      return ((PDFDictionary) pageReference).getPageNumber ();
    if (pageReference == null)
      {
        Options.warn ("null page reference");
        return -1;
      }
    throw new Error ("invalid page reference " + pageReference);
  }

  public String getType ()
  {
    return array.nameAt (1);
  }
  
  public double [] getCoordinates ()
  {
    double [] coors = new double [array.size () - 2];
    for (int i = 0;i < coors.length;i++)
      coors [i] = array.doubleAt (i + 2);
    return coors;
  }

  public static PDFDestination resolveDestination
    (PDFObject destination,PDFDocument document)
  {
    if (destination == null)
      return null;
    if (destination instanceof PDFString)
      {
        PDFDictionary dests = document.getNamesEntry ("Dests");
        if (dests == null) // in SVGTest.pdf
          {
            Options.warn ("named destinations without destination name tree ignored");
            return null;
          }
        destination = dests.treeLookup ((PDFString) destination);
      }
    else if (destination instanceof PDFName)
      destination = ((PDFDictionary) document.root.get ("Dests")).
        get ((PDFName) destination);
    // The spec doesn't allow dictionaries as explicit destinations, only
    // as values of named destinations, but iia200402-ae.pdf contains
    // an explicit dictionary and Acrobat and ghostscript accept it.
    if (destination instanceof PDFDictionary)
      destination = ((PDFDictionary) destination).get ("D");
    // PDFRef.pdf, of all files, contains a "null" entry in
    // the List of Figures for Figure 4.29, and PDFReference15_v6.pdf
    // contains an undefined named destination on raw page 96
    if (destination == null)
      return null;
    return new PDFDestination ((PDFArray) destination);
  }
}
