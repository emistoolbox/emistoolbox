/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt;

import java.awt.Frame;
import java.awt.FileDialog;

public class SimpleFileDialog extends FileDialog
{
  public SimpleFileDialog ()
  {
    super (new Frame ());
  }

  public SimpleFileDialog (Frame frame)
  {
    super (frame);
  }

  public SimpleFileDialog (Frame frame,String title)
  {
    super (frame,title);
  }

  public String getPathname (int mode)
  {
    setMode (mode);
    setVisible (true);
    String file = getFile ();
    String dir = getDirectory ();
    dispose ();
    if (file == null || dir == null)
      return null;
    return dir + file;
  }

  public String getPathname (int mode,String title)
  {
    setTitle (title);
    return getPathname (mode);
  }

  public String getPathname (int mode,String title,String defaultName)
  {
    setDirectory (System.getProperty ("user.dir"));
    setFile (defaultName);
    return getPathname (mode,title);
  }
}

