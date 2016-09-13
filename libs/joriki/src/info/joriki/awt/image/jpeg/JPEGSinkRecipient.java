/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import info.joriki.io.filter.SinkRecipient;

public interface JPEGSinkRecipient extends JPEGSpeaker,SinkRecipient
{
  JPEGFormat getFormat ();
}
