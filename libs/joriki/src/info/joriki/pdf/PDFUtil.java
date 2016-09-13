/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

public class PDFUtil
{
  private PDFUtil () {}

  public static Set getAdditionalActions (PDFDictionary dictionary,PDFDocument document)
  {
  /* Additional actions for page objects are no longer inheritable since PDF 1.3,
   * see implementation note 31 for Section 3.6.2. See also comments in
   * SVGContentStreamHandler and PDFAnnotation about the widget/field ambiguity.
   */
    PDFDictionary additionalActionsDictionary = (PDFDictionary) dictionary.get ("AA");
    if (additionalActionsDictionary == null)
      return null;
    Set additionalActions = new HashSet ();
    Iterator actions = additionalActionsDictionary.keys ().iterator ();
    while (actions.hasNext ())
      {
        PDFAdditionalAction action = new PDFAdditionalAction ();
        action.key = (String) actions.next ();
        action.action = new PDFAction
          ((PDFDictionary) additionalActionsDictionary.get (action.key),document);
        additionalActions.add (action);
      }
    return additionalActions;
  }

  public static int getInitialPageNumber (PDFDocument document)
  {
    PDFObject openAction = document.root.get ("OpenAction");
    return
      openAction == null ? 1 :
      (openAction instanceof PDFDictionary ?
       new PDFAction ((PDFDictionary) openAction,document).getDestination () :
       new PDFDestination ((PDFArray) openAction)).getPageNumber ();
  }
}  
