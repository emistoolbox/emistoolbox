/*
 * Copyright 2007 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.Assertions;
import info.joriki.util.General;
import info.joriki.util.NotImplementedException;

public class PageLabeler {
  PDFDictionary pageLabels;
  String prefix;
  int pageNumber;
  int numeric;
  char style;

  public PageLabeler (PDFDocument document) {
    pageLabels = (PDFDictionary) document.root.get ("PageLabels");
  }

  public String nextLabel () {
    if (pageLabels == null)
      return null;

    PDFDictionary pageLabelDictionary = (PDFDictionary) pageLabels.treeLookup (new PDFInteger (pageNumber++));
    // This shouldn't happen, since the tree is supposed to
    // contain an entry for page index 0 (PDF spec p. 73 (89)),
    // but it does in NDSforPJAE_UsersGuide02-dirty.pdf
    if (pageNumber == 1 && pageLabelDictionary == null) {
      // give up on page labels
      pageLabels = null;
      return null;
    }

    if (pageLabelDictionary != null) {
      Assertions.expect (pageLabelDictionary.isOptionallyOfType ("PageLabel"));
      prefix = pageLabelDictionary.getTextString ("P");
      numeric = pageLabelDictionary.getInt ("St",1);
      Assertions.expect (numeric >= 1);
      String styleString = pageLabelDictionary.getName ("S");
      style = styleString == null ? 0 : styleString.charAt (0);
      pageLabelDictionary.checkUnused ("8.6");
    }

    StringBuilder labelBuilder = new StringBuilder ();
    if (prefix != null)
      labelBuilder.append (prefix);
    switch (style)
      {
      case 0   : break;
      case 'n' : break;
      case 'D' : labelBuilder.append (numeric); break;
      case 'r' : labelBuilder.append (General.toRomanNumerals (numeric).toLowerCase ()); break;
      case 'R' : labelBuilder.append (General.toRomanNumerals (numeric).toUpperCase ()); break;
      case 'a' :
      case 'A' :
        char c = (char) (style + (numeric - 1) % 26);
        for (int i = 0;i <= (numeric - 1) / 26;i++)
          labelBuilder.append (c);
        break;
      default : throw new NotImplementedException ("page label style " + style);
      }
    numeric++;
    return labelBuilder.toString ();
  }
}
