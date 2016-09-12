/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

/**
   A converter is a filter with two active ends. For many encoders and
   decoders, this is the most natural mode of operation, since having
   arbitrary amounts of data written to or read from a codec often
   results in a need for buffering or state memory. In this package,
   buffering is provided by generic buffer classes external to the
   converters. Since a converter is not passive at either end, it is
   never prompted to act by read or write requests. It must therefore
   be cranked from the outside.
   @see Crank
   @see CompositeConverter
*/

public interface Converter extends SinkRecipient,SourceRecipient {}


