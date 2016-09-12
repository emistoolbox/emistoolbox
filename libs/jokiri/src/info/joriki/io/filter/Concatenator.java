/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

/**
   The <code>Concatenator</code> class is a filter factory with
   static methods that concatenate any admissible combination
   of two filters to form a new filter. A combination is admissible
   iff it matches a passive output with an active input or an
   active output with a passive input, i.e. a source provider with
   a source receiver or a sink receiver with a sink provider.
   The following table shows the admissible combinations and
   the types of the resulting composite filters. The rows are
   labeled by the component on the input side, the columns
   by the component on the output side of the composite filter.
   <P>
   <table align=center border=1>
   <tr align=center>
   <td></td>
   <td></td>
   <td>{@link Converter}</td>
   <td>{@link OutputFilter}</td>
   <td>{@link InputFilter}</td>
   <td>{@link Buffer}</td>
   </tr>
   <tr></tr>
   <tr align=center>
   <td>{@link Converter}</td>
   <td></td>
   <td>x</td>
   <td>{@link Converter}</td>
   <td>x</td>
   <td>{@link InputFilter}</td>
   </tr>
   <tr align=center>
   <td>{@link OutputFilter}</td>
   <td></td>
   <td>x</td>
   <td>{@link OutputFilter}</td>
   <td>x</td>
   <td>{@link Buffer}</td>
   </tr>
   <tr align=center>
   <td>{@link InputFilter}</td>
   <td></td>
   <td>{@link Converter}</td>
   <td>x</td>
   <td>{@link InputFilter}</td>
   <td>x</td>
   </tr>
   <tr align=center>
   <td>{@link Buffer}</td>
   <td></td>
   <td>{@link OutputFilter}</td>
   <td>x</td>
   <td>{@link Buffer}</td>
   <td>x</td>
   </tr>
   </table>
   <P>
   Note that concatenation is associative with respect to the external
   function of the composite filter, but not with respect to its
   inner workings. That is, a given filter chain can be constructed
   in various ways depending on the order in which binary concatenations
   are performed; the resulting filters will appear functionally identical
   but may differ in efficiency, since their constituents are wired up
   in different ways and data may be buffered in different places along
   the chain.
   <P>
   Note also that any connecting of sources and sinks and setting of
   cranks is performed at the time the chain is constructed; during
   the actual filtering process, only the atomic constituents of the
   chain communicate with each other, with no efficiency penalty from
   the hierarchical nature of the chain construction.
   <P>
   Note also that while input filters and output filters can be
   constructed from appropriate combinations of converters and
   buffers, converters and buffers can only be constructed by
   appending or prepending a filter to an object of the same type.
   Thus, every composite buffer contains an atomic buffer, and
   every composite converter contains an atomic converter.
*/
   
public class Concatenator
{
  private Concatenator () {}

  final static Crank getCrank (Converter converter)
  {
    return converter instanceof Crank ? (Crank) converter : ((CompositeConverter) converter).getCrank ();
  }

  /*
    The combinations
    buffer * converter -> output filter
    and
    converter * buffer -> input filter
    are the only non-trivial ones.
    In the first case, the converter is set as the buffer's output crank,
    so that when data is written to its input side, the buffer makes sure
    the converter processes it and passes it through to the output side.
    In the second case, the converter is set as the buffer's input crank,
    so that when data is read from its output side, the buffer makes sure
    the converter processes data and writes it to the buffer to be read.
  */

  /**
     Concatenates a buffer and a converter to form an output filter.
     The buffer is set up to crank the converter when data has been
     written to its input end.
  */
  public static OutputFilter concatenate (final Buffer buffer,final Converter converter)
  {
    buffer.setCrank (getCrank (converter),false);
    converter.setSource (buffer.getSource ());
    return new OutputFilter () {
        public Object getSink () { return buffer.getSink (); }
        public void setSink (Object sink) { converter.setSink (sink); }
      };
  }
  
  /**
     Concatenates a converter and a buffer to form an input filter.
     The buffer is set up to crank the converter when data is read
     from its output end.
  */
  public static InputFilter concatenate (final Converter converter,final Buffer buffer)
  {
    buffer.setCrank (getCrank (converter),true);
    converter.setSink (buffer.getSink ());
    return new InputFilter () {
        public Object getSource () { return buffer.getSource (); }
        public void setSource (Object source) { converter.setSource (source); }
      };
  }

  /**
     Appends an input filter to a buffer to form a new buffer.
  */
  public static Buffer concatenate (final Buffer buffer,final InputFilter filter)
  {
    filter.setSource (buffer.getSource ());
    return new Buffer () {
        public Object getSource () { return filter.getSource (); }
        public Object getSink () { return buffer.getSink (); }
        public void setCrank (Crank crank,boolean input) { buffer.setCrank (crank,input); }
      };
  }

  /**
     Prepends an output filter to a buffer to form a new buffer.
  */
  public static Buffer concatenate (final OutputFilter filter,final Buffer buffer)
  {
    filter.setSink (buffer.getSink ());
    return new Buffer () {
        public Object getSink () { return filter.getSink (); }
        public Object getSource () { return buffer.getSource (); }
        public void setCrank (Crank crank,boolean which) { buffer.setCrank (crank,which); }
      };
  }

  /**
     Concatenates two input filters to form a new input filter.
     This is analogous to code like<BR><code>new DataInputStream
     (new FileInputStream (file))</code>.
  */
  public static InputFilter concatenate (final InputFilter filter1,final InputFilter filter2)
  {
    filter2.setSource (filter1.getSource ());
    return new InputFilter () {
        public Object getSource () { return filter2.getSource (); }
        public void setSource (Object source) { filter1.setSource (source); }
      };
  }

  /**
     Concatenates two output filters to form a new output filter.
     This is analogous to code like<BR><code>new DataOutputStream
     (new FileOutputStream (file))</code>.
  */
  public static OutputFilter concatenate (final OutputFilter filter1,final OutputFilter filter2)
  {
    filter1.setSink (filter2.getSink ());
    return new OutputFilter () {
        public Object getSink () { return filter1.getSink (); }
        public void setSink (Object sink) { filter2.setSink (sink); }
      };
  }

  /**
     Appends and output filter to a converter to form a new converter.
  */
  public static CompositeConverter concatenate (final Converter converter,final OutputFilter filter)
  {
    converter.setSink (filter.getSink ());
    return new CompositeConverter () {
        public void setSource (Object source) { converter.setSource (source); }
        public void setSink (Object sink) { filter.setSink (sink); }
        public Crank getCrank () { return Concatenator.getCrank (converter); }
      };
  }

  /**
     Prepends an input filter to a converter to form a new converter.
  */
  public static CompositeConverter concatenate (final InputFilter filter,final Converter converter)
  {
    converter.setSource (filter.getSource ());
    return new CompositeConverter () {
        public void setSource (Object source) { filter.setSource (source); }
        public void setSink (Object sink) { converter.setSink (sink); }
        public Crank getCrank () { return Concatenator.getCrank (converter); }
      };
  }

  /*
    public static Filter concatenate (Filter [] filters)
    {
    Filter filter = filters [0];
    for (int i = 1;i < filters.length;i++)
    filter = concatenate (filter,filters [i]);
    return filter;
    }
  */
}
