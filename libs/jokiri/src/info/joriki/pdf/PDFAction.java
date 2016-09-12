/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;
import info.joriki.util.NotTestedException;

public class PDFAction
{
  public String type;
  public PDFDictionary actionDictionary;
  public PDFAction nextAction;
  PDFDocument dad;

  public PDFAction (PDFDictionary actionDictionary,PDFDocument dad)
  {
    this.actionDictionary = actionDictionary;
    this.dad = dad;
    Assertions.expect (actionDictionary.isOptionallyOfType ("Action"));
    type = actionDictionary.getName ("S");
    PDFDictionary nextActionDictionary = (PDFDictionary) actionDictionary.get ("Next");
    if (nextActionDictionary != null)
      nextAction = new PDFAction (nextActionDictionary,dad);
  }

  public int getRemotePageNumber () throws IOException
  {
    Assertions.expect (type,"GoToR");
    PDFObject object = actionDictionary.get ("D");
    if (object instanceof PDFArray)
      return ((PDFArray) object).intAt (0) + 1;
    if (object instanceof PDFString)
      {
        PDFFile file = new PDFFile
          (new PDFFileSpecification (actionDictionary.get ("F")).getFile ());
        try {
          return PDFDestination.resolveDestination
            (actionDictionary.get ("D"),file.getDocument ()).getPageNumber ();
        } finally { file.close (); }
      }
    throw new Error ("invalid destination " + object);
  }

  public PDFDestination getDestination ()
  {
    Assertions.expect (type,"GoTo");
    return PDFDestination.resolveDestination (actionDictionary.get ("D"),dad);
  }

  public String getURI ()
  {
    Assertions.expect (type,"URI");
    if (actionDictionary.getBoolean ("IsMap",false))
      throw new NotImplementedException ("URI map");
    String URI = actionDictionary.getAsciiString ("URI"); 
    PDFDictionary URIdictionary = (PDFDictionary) dad.getRoot ().get ("URI");
    if (URIdictionary == null)
      return URI;
    String base = URIdictionary.getAsciiString ("Base");
    URIdictionary.checkUnused ("8.49");
    throw new NotTestedException ();
//  return base + URI;
  }
}
