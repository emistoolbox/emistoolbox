/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

/**
   A sink recipient is a filter that requires a data sink
   to write to, i&#046;e&#046; a filter with an active output end.
   @see SinkProvider
*/
public interface SinkRecipient extends Filter
{
  /**
     Sets the sink that this filter writes to. Typically, a composite
     filter will pass this call through to its component on the output
     end, whereas an atomic filter will set itself up to write to the
     sink provided.
     @param sink the sink to be written to
  */
  public void setSink (Object sink);
}
