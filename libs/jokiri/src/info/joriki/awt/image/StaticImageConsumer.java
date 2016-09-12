/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

public abstract class StaticImageConsumer extends AbstractImageConsumer
{
  boolean done = false;

  public void imageComplete (int status)
  {
    if (done) // ignore the error that's sent because we don't know how to deregister ourselves from the producer
      return;
    super.imageComplete (status);
    if (status == SINGLEFRAMEDONE)
      done = true;
    if (status == STATICIMAGEDONE)
      done = true;
  }
}
