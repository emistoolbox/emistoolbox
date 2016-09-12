/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;

import java.awt.Toolkit;

public class SimpleClipboard implements ClipboardOwner {
  public void setClipboardContents (String string) {
    Toolkit.getDefaultToolkit ().getSystemClipboard ().setContents (new StringSelection (string),this);
  }

  public String getClipboardContents () {
    try {
      return (String) Toolkit.getDefaultToolkit ().getSystemClipboard ().getContents (null).getTransferData (DataFlavor.stringFlavor);
    } catch (Exception e) {
      e.printStackTrace ();
      return null;
    }
  }

  public void lostOwnership (Clipboard clipboard,Transferable contents) {}
}
