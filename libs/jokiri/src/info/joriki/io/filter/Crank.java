/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

import java.io.IOException;

/**
   A crank is an atomic converter. It is active at both ends,
   and it can be cranked from the outside to make it process data.
*/
public interface Crank extends Converter
{
  /**
     Cranks this crank once. The crank should perform
     a single atomic processing step in response to this call;
     it is the responsibility of the cranker to call this method
     repeatedly as long as data is available or required, respectively.
     A loop inside the crank would be OK in the case of a crank
     cranked due to data becoming available, since the crank could
     go on until it encounters {@link Filter#EOD EOD}.
     However, in the case of a crank
     cranked due to requests for data, the crank itself cannot know
     when to stop, and so would go on producing data that would
     unnecessarily need to buffered, or in the worst case read
     more data than it should.
     @return
     <table><tr valign=top><td>
     {@link Filter#OK OK}
     </td><td>
     if an atomic processing step was performed
     </td></tr><tr valign=top><td>
     {@link Filter#EOI EOI}
     </td><td>
     if the converter encountered <code>EOI</code> on its input end or has determined that the data conversion is complete<BR>
     </td></tr><tr valign=top><td>
     {@link Filter#EOD EOD}
     </td><td>
     if the converter encountered <code>EOD</code> on its
     input end and needs more data to proceed
     </td></tr></table>
  */
  public int crank () throws IOException;
}
