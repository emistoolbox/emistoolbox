/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import java.io.IOException;

import java.awt.image.ColorModel;
import java.awt.image.ImageProducer;
import java.awt.image.ImageConsumer;

import java.util.Vector;
import java.util.Hashtable;

import info.joriki.util.NotImplementedException;

public abstract class AbstractImageProducer implements ImageProducer, ImageProperties
{
  protected Vector consumerVector = new Vector ();
  protected ImageConsumer [] consumers;
  
  public void addConsumer (ImageConsumer consumer)
  {
    consumerVector.addElement (consumer);
  }

  public boolean isConsumer (ImageConsumer consumer)
  {
    return consumerVector.contains (consumer);
  }

  public void removeConsumer (ImageConsumer consumer)
  {
    consumerVector.removeElement (consumer);
  }

  public void requestTopDownLeftRightResend () {}

  public void startProduction (ImageConsumer consumer)
  {
    addConsumer (consumer);
    snapshotConsumers ();
    try {
      produceImage ();
    } catch (IOException ioe) {
      ioe.printStackTrace ();
      throw new Error ("IO exception during image production");
    }
  }

  protected void staticImageDone ()
  {
    for (int i = 0;i < consumers.length;i++)
      consumers [i].imageComplete (ImageConsumer.STATICIMAGEDONE);
    // According to the API, consumers should remove themselves if they're not interested
    // in successive frames, so signal them an error if they haven't. This is what the
    // image producers producing static images obtained by <code>getImage</code> do.
    snapshotConsumers ();
    for (int i = 0;i < consumers.length;i++)
      consumers [i].imageComplete (ImageConsumer.IMAGEERROR);
  }

  protected void snapshotConsumers ()
  {
    consumers = new ImageConsumer [consumerVector.size ()];
    consumerVector.copyInto (consumers);
  }

  public void requestTopDownLeftRightResend (ImageConsumer consumer)
  {
    throw new NotImplementedException ("top down left right resend");
  }

  protected void setParameters (int width,int height,
                                ColorModel colorModel,
                                int hints,
                                Hashtable properties)
  {
    for (int i = 0;i < consumers.length;i++)
      {
        ImageConsumer consumer = consumers [i];
        consumer.setHints (hints);
        consumer.setDimensions (width,height);
        consumer.setColorModel (colorModel);
        if (properties != null)
          consumer.setProperties (properties);
      }
  }

  abstract protected void produceImage () throws IOException;
}
