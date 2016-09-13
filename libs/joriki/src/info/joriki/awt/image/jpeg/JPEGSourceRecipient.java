/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import info.joriki.io.filter.SourceRecipient;

public interface JPEGSourceRecipient extends JPEGSpeaker,SourceRecipient
{
  void setFormat (JPEGFormat format);
}
