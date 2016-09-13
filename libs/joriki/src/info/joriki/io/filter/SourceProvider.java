/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

/**
   A source provider provides a data source, which is analogous to an
   input stream in the <code>java.io</code> package. Thus, a source
   provider is a filter that can be read from,
   i.e. a filter with a passive output end.
   @see SourceRecipient
*/
public interface SourceProvider extends Filter
{
  /**
     Returns the source provided by this filter. Typically, a composite
     filter will pass this call through to its component on the input
     end, whereas an atomic filter will return itself or an inner class
     that acts as a source.
     @return the source provided by this filter
  */
  public Object getSource ();
}
