/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

import java.io.IOException;

/**
   This is the abstract base class for an atomic buffer. Any buffer
   that actually buffers data (as opposed to a composite buffer
   that is constructed by appending an input filter or prepending
   an output filter to an existing buffer) will typically subclass
   this class. Bit buffers and byte buffers are currently implemented,
   but in principle any kind of data could be buffered.
*/
public abstract class AtomicBuffer implements Buffer
{
  /**
     The crank that needs to be cranked when a read request
     cannot be satisfied with the data currently available
  */
  protected Crank inputCrank = null;

  /**
     The crank that needs to be cranked when data becomes
     available by this buffer being written to
  */
  protected Crank outputCrank = null;

  /**
     The number of data items currently in the buffer
  */
  protected int len = 0;

  protected AtomicBuffer () {}

  public void setCrank (Crank crank,boolean input)
  {
    if (input)
      inputCrank = crank;
    else
      outputCrank = crank;
  }

  /**
     A subclass should call this method after data has been
     written to it. If a crank has been set for the
     output side of this buffer it will be cranked, prompting
     it to read the data that has become available.
     @exception IOException if the crank throws an
     <code>IOException</code> upon being cranked
  */
  final protected void outputCrank () throws IOException
  {
    // crank the output crank, if any, as long as
    // it claims to have done some processing
    if (outputCrank != null)
      {
        int result;
        while ((result = outputCrank.crank ()) == OK)
          ;
        // if the output crank signals EOI, throw an EOIException
        // if there is someone waiting to catch it.
        if (result == EOI)
          new EOIException ().throwAt ("AtomicBuffer.inputCrank");
      }
  }

  /**
     A subclass should call this method if it cannot
     satisfy a read request. If a crank has been set for
     the input side of this buffer it will be cranked, prompting
     it to provide the requested data.
     @param n the number of additional data items required to fulfill
     the read request. 
     @return
     <table><tr valign=top><td>
     {@link Filter#OK OK}
     </td><td>
     if the requested data were available or could be
     provided by the input crank
     </td></tr><tr valign=top><td>
     {@link Filter#EOI EOI}
     </td><td>
     if the input crank was not able to provide the requested data
     </td></tr><tr valign=top><td>
     {@link Filter#EOD EOD}
     </td><td>
     if the requested data were not available and
     this buffer has no input crank to crank
     </td></tr></table>
     @exception IOException if the crank throws an
     <code>IOException</code> upon being cranked
  */
  boolean closed = false;
  boolean chainDone = false;
  final protected int inputCrank (int n) throws IOException
  {
    // crank the input crank, if any, as long as it hasn't
    // produced enough data and hasn't run into trouble
    if (inputCrank != null && !(chainDone || closed))
      try {
        while (len < n && inputCrank.crank () == OK)
          ;
      } catch (EOIException eoie) {
        chainDone = true;
      }
    // if there is an input crank, then it should be able to
    // produce more data, and the fact that it couldn't means
    // EOI. If there isn't one however, there's nothing to
    // worry about; there just isn't any more data currently,
    // so return EOD.
    return len >= n ? OK : inputCrank != null || closed ? EOI : EOD;
  }

  /**
   * Resets this buffer.
   */
  public void reset ()
  {
    closed = chainDone = false;
    len = 0;
  }

  /**
   *  Closes this buffer.
     @exception IOException  if an I/O error occurs
   */
  public void close () throws IOException
  {
    closed = true;
    outputCrank ();
  }

  /**
     Returns the source provided by this buffer, namely the buffer itself.
     @return the source provided by this buffer
  */
  public Object getSource () { return this; }
  /**
     Returns the sink provided by this buffer, namely the buffer itself.
     @return the sink provided by this buffer
  */
  public Object getSink   () { return this; }
}
