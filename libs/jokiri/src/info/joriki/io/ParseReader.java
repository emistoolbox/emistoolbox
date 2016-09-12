/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.Reader;

public class ParseReader extends LineNumberReader
{
  String filename;

  public ParseReader (Reader reader,String filename)
  {
    super (reader);
    this.filename = filename;
  }

  public ParseReader (String filename) throws IOException
  {
    this (new FileReader (filename),filename);
  }

  public ParseReader (File file) throws IOException
  {
    this (new FileReader (file),file.getName ());
  }

  public void throwException (String message)
  {
    throw new RuntimeException (message + " on line " + getLineNumber () + " in file " + filename);
  }
}
