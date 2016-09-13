/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.HashMap;
import java.util.Map;

public interface OptionalContent {
  String ON = "ON";
  String OFF = "OFF";
  PDFName VIEW = new PDFName ("View");
  Map<PDFDictionary,PDFBoolean> states = new HashMap<PDFDictionary, PDFBoolean> ();
}
