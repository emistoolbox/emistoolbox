/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

/**
   A source recipient is a filter that requires a data source
   to read from, i&#046;e&#046; a filter with an active input end.
   @see SourceProvider
*/
public interface SourceRecipient extends Filter
{
  /**
     Sets the source that this filter reads from. Typically, a composite
     filter will pass this call through to its component on the input
     end, whereas an atomic filter will set itself up to read from the
     source provided.
     @param source the source to be read from
  */
  public void setSource (Object source);
}
