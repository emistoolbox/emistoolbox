/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.List;

import info.joriki.util.NotImplementedException;
import info.joriki.util.NotTestedException;
import info.joriki.util.Options;

public class OptionalContentVisibilityPolicy implements OptionalContent {
  boolean state; //  On/Off
  boolean any;   // Any/All
  
  OptionalContentVisibilityPolicy (String policy) {
    if (!policy.equals ("AnyOn"))
      throw new NotTestedException ("optional content visibility policy " + policy);
    for (int i = 0;i < 2;i++)
      for (int j = 0;j < 2;j++)
        if (policy.equals (new String [] {"All","Any"} [i] + new String [] {"Off","On"} [j]))
        {
          any = i == 1;
          state = j == 1;
          return;
        }
    throw new NotImplementedException ("optional content visibility policy " + policy);
  }

  public boolean getVisibility (List<PDFDictionary> groupDictionaries) {
    // This case is explicitly discussed in Table 4.46; generalized quantifiers would give a different result.
    if (groupDictionaries.isEmpty ())
      return true; // I guess this is what the spec means by "no effect on the visibility of any content".
    for (PDFDictionary groupDictionary : groupDictionaries) {
      PDFBoolean dictionaryState = states.get (groupDictionary);
      // If a group dictionary doesn't have a state entry, that means it didn't get set up when the document
      // was loaded, either because the document catalog doesn't have an optional content properties dictionary
      // or because the optional content properties dictionary's optional content group array doesn't list this
      // optional content group. In the former case, the spec says to ignore all optional content structures --
      // I presume that means to treat everything as visible. It doesn't say what to do if an optional content
      // group isn't listed in the optional content properties dictionary's optional content group array, but
      // it does say "Every optional content group must be included in this array", so the behavior if it isn't
      // is undefined, and treating the content as visible is probably a good guess.
      if (dictionaryState == null) {
        Options.warn ("uninitialized optional content state");
        return true;
      }
      if (dictionaryState.booleanValue () ^ state ^ any)
        return any;
    }
    return !any;
  }
}
