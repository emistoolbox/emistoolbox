/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.List;
import java.util.ArrayList;

import info.joriki.util.Assertions;

public class OptionalContentMembership {
  private List<PDFDictionary> groupDictionaries = new ArrayList<PDFDictionary> ();
  private OptionalContentVisibilityPolicy visibilityPolicy;
  // Both in content streams (using content marked with an /OC tag)
  // and in XObjects and annotations (using an OC entry), optional
  // content membership can be specified either by an optional content
  // group dictionary or by an optional content membership dictionary.
  // This constructor takes both kinds of dictionaries and automagically
  // turns an optional content group into an optional content membership
  // with the group as the only member and a visibility policy of AnyOn
  // (equivalent in this case to AllOn).
  public OptionalContentMembership (PDFDictionary dictionary)
  {
    if (dictionary.isOfType ("OCMD"))
    {
      PDFObject groups = dictionary.get ("OCGs");
      if (groups instanceof PDFDictionary)
        addGroupDictionary ((PDFDictionary) groups);
      else if (groups instanceof PDFArray)
      {
        PDFArray groupArray = (PDFArray) groups;
        for (int i = 0;i < groupArray.size ();i++)
        {
          PDFDictionary groupDictionary = (PDFDictionary) groupArray.get (i);
          if (groupDictionary != null)
            addGroupDictionary (groupDictionary);
        }
      }
      else if (groups != null)
        throw new IllegalArgumentException ("optional content group specification " + groups.getClass ());
      
      visibilityPolicy = new OptionalContentVisibilityPolicy (dictionary.getName ("P","AnyOn"));
      dictionary.checkUnused ("4.46");
    }
    else if (dictionary.isOfType ("OCG")) {
      addGroupDictionary (dictionary);
      visibilityPolicy = new OptionalContentVisibilityPolicy ("AnyOn");
    }
    else
      throw new IllegalArgumentException ("optional content membership specification " + dictionary.getName ("Type"));
  }
  
  private void addGroupDictionary (PDFDictionary groupDictionary) {
    Assertions.expect (groupDictionary.isOfType ("OCG"));
    groupDictionaries.add (groupDictionary);
  }

  public boolean isVisible () {
    return visibilityPolicy.getVisibility (groupDictionaries);
  }
}
