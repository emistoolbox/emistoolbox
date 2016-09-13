/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.File;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

public class PDFFileSpecification
{
  File file;

  public PDFFileSpecification (PDFObject object)
  {
    if (object instanceof PDFDictionary)
      {
        PDFDictionary dictionary = (PDFDictionary) object;
        Assertions.expect (dictionary.isOfType ("FileSpec") ||
                           dictionary.isOfType ("Filespec"));
        object = dictionary.get ("F");
        dictionary.checkUnused ("3.40");
        Assertions.unexpect (object,null);
      }
    if (object instanceof PDFString)
      {
        byte [] name = ((PDFString) object).getBytes ();
        for (int begin = 0,end = 0;begin < name.length;begin = ++end)
          {
            boolean backslash = false;
            while (end != name.length && (name [end] != '/' || backslash))
              {
                byte b = name [end++];
                backslash = b == '\\';
                Assertions.unexpect (b,'<');
              }
            file = new File (file,new String (name,begin,end - begin));
          }
        if (file == null)
          file = new File (".");
      }
    else
      throw new NotImplementedException ("file specification " + object.getClass ());
  }

  public File getFile ()
  {
    return file;
  }

  public String toString ()
  {
    return file.toString ();
  }
}
