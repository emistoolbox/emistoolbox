/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.HashMap;

import info.joriki.util.Extensions;
import info.joriki.util.Options;
import info.joriki.util.ArgumentIterator;
import info.joriki.util.Switch;

public class PDFDiff
{
  public final static Switch diffStreams = new Switch ("print diff of differing streams");
  public final static Switch increment = new Switch ("obtain filename B by incrementing filename A");
  public final static Switch series = new Switch ("use series of incremented file names");
  public final static Switch interpolate = new Switch ("generate files that interpolate between the input files");
  
  public static void main (String [] args) throws IOException
  {
    ArgumentIterator arguments = new Options (PDFDiff.class,"<file A> [<file B>]",
    "compares two or more PDF files and lists the differences").parse (args);

    String fileA = arguments.nextString ();

    try {
      do {
        String fileB = increment.isSet () || series.isSet () ?
          Extensions.incrementFileName (fileA) :
          arguments.nextString ();
        
        PDFDocument documentA = new PDFFile (fileA).getDocument ();
        PDFDocument documentB = new PDFFile (fileB).getDocument ();
        
        System.out.println ();
        System.out.println ("diff " + fileA + " " + fileB);

        PDFContainer.compared.set (new HashMap<PDFObject, PDFObject> ());
        documentA.getRoot ().diff (documentB.getRoot (),"root");
        if (interpolate.isSet ())
          documentA.interpolate (documentB,"interpolate");
        fileA = fileB;
      } while (series.isSet ());
    } catch (FileNotFoundException fnfe) {
      if (!series.isSet ())
        throw fnfe;
    }
  }
}
