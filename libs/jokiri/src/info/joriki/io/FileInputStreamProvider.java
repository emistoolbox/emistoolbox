/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A file input stream provider provides input streams that read
 * from a specified file.
 */
public class FileInputStreamProvider implements InputStreamProvider
{
  public File file;

  /**
   * Creates a file input stream provider that reads from the file
   * with the specified name.
   * @param name the name of the file to be read from
   */
  public FileInputStreamProvider (String name)
  {
    this (new File (name));
  }

  /**
   * Creates a file input stream provider that reads from the specified file.
   * @param name the file to be read from
   */
  public FileInputStreamProvider (File file)
  {
    this.file = file;
  }

  /**
   * Provides an input stream that reads from the file associated
   * with this input stream provider.
   * @return an input stream that reads from the file associated
   * with this input stream provider
   * @exception IOException if an I/O error occurs
   */
  public InputStream getInputStream () throws IOException
  {
    return new ReadableFileInputStream (file);
  }

  public byte [] toByteArray () throws IOException
  {
    return Util.undump (file);
  }
}
