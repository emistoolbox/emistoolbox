/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.Assertions;

public class ContentMark {
  String tag;
  PDFDictionary properties;
  
  ContentMark (PDFName tagName) {
    this (tagName,null);
  }
  
  ContentMark (PDFName tagName,PDFDictionary properties) {
    this.tag = tagName.getName ();
    this.properties = properties;
    
    if (tag.equals ("OC") && // TextCafePDFGuidelines070202.pdf
        // The spec explicitly notes (in Section 4.10) that OC
        // can be used otherwise and is only for optional content
        // if the properties dictionary is a valid optional content
        // group or membership dictionary. pmnetwork200704emea_7.pdf
        // and pmnetwork200604_7.pdf contain another use.
        // Page 1 of SH101Newsletter200705.pdf contains a use of OC
        // with BMC, which leads to a call with properties == null,
        // and page 10 of SH101200710.pdf uses a name for an optional
        // content properties dictionary that isn't in the resource
        // dictionary's Properties dictionary and thus also leads
        // to a call with properties == null.
        properties != null &&
        (properties.isOfType ("OCMD") ||
         properties.isOfType ("OCG")))
      // Creating a new optional content membership object every time
      // is a bit wasteful -- if optional content is used more often,
      // we might want to cache these or manage them centrally.
      Assertions.expect (new OptionalContentMembership (properties).isVisible ());
  }
  
}
