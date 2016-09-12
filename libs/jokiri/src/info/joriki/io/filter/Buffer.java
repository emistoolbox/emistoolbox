/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

/**
   A buffer is a filter with two passive ends. If it is written to or
   read from, it must prompt the next filter in the chain to process
   the input that has become available or to produce the output that
   has been requested, respectively.
*/

public interface Buffer extends SinkProvider, SourceProvider
{
  /**
     Sets the crank that the buffer needs to crank when it is written
     to or read from. When a buffer is inserted into a chain, it will
     have exactly one of its cranks set.
     @param crank The crank to be cranked when data arrives or is requested
     @param input
     <table><tr><td>
     <code>true</code>
     </td><td>
     if the crank on the input end of the buffer is to be set
     </td></tr><tr><td>
     <code>false</code>
     </td><td>
     if the crank on the output end of the buffer is to be set
     </td></tr></table>
     @see Concatenator
  */
  public void setCrank (Crank crank,boolean input);
}
