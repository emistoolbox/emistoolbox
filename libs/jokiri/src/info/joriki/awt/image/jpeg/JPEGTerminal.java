/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

abstract public class JPEGTerminal implements JPEGSourceRecipient
{
  JPEGSource source;

  public void setSource (Object source)
  {
    this.source = (JPEGSource) source;
  }
}
