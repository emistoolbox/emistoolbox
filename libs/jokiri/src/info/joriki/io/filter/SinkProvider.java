/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

/**
   A sink provider provides a data sink, which is analogous to an output
   stream in the <code>java.io</code> package.
   Thus, a sink provider is a filter that can be written to,
   i.e. a filter with a passive input end.
   @see SinkRecipient
*/
public interface SinkProvider extends Filter
{
  /**
     Returns the sink provided by this filter. Typically, a composite
     filter will pass this call through to its component on the output
     end, whereas an atomic filter will return itself or an inner class
     that acts as a sink.
     @return the sink provided by this filter
  */
  public Object getSink ();
}
