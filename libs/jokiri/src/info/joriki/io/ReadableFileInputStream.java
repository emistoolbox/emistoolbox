/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ReadableFileInputStream extends FileInputStream implements Readable
{
  public ReadableFileInputStream (String string) throws FileNotFoundException
  {
    super (string);
  }

  public ReadableFileInputStream (File file) throws FileNotFoundException
  {
    super (file);
  }
}
