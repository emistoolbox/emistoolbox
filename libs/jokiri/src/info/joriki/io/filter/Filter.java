/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

/**
   A filter is an object with an input end and an output end,
   both of which can be either active or passive. For instance, a filter
   with an active input end reads its input from another object, while a
   filter with a passive input end receives input when another object writes
   to it. This leads to four different combinations, summarized in the
   following table:
   <P>
   <table align=center border=1>
   <tr align=center>
   <td></td>
   <td>active input</td>
   <td>passive input</td>
   </tr>
   <tr align=center>
   <td>active output</td>
   <td>{@link Converter}</td>
   <td>{@link OutputFilter}</td>
   </tr>
   <tr align=center>
   <td>passive output</td>
   <td>{@link InputFilter}</td>
   <td>{@link Buffer}</td>
   </tr align=center>
   </table>
   <P>
   The type of input or output is arbitrary. For instance, a filter
   might take bits as input and produce bytes as output. Other types
   of input and output are allowed, but only bits and bytes have been
   implemented so far.
   <P>
   Filters can be concatenated to form arbitrary chains, subject only
   to the requirement that active and passive ends are properly matched.
   @see Concatenator
*/

public interface Filter
{
  /** A return value indicating that everything is OK. */
  int OK = 0;

  /** A return value indicating that a filter has reached
      the end of its input, either by reading an <code>EOI</code>
      itself or according to some internal criterion for the
      end of a stream. The value of this field is <code>-1</code>,
      so that an <code>InputStream</code> can be used as a source. */
  int EOI = -1;

  /** A return value indicating that no more data is currently
      available. */
  int EOD = -2;
}
