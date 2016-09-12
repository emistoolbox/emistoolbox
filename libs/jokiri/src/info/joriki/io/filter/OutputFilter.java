/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

/**
   An output filter is similar to a filter output stream in the
   <code>java.io</code> package. It has an active output end
   and a passive input end, i.e. it writes and is written to.
*/
public interface OutputFilter extends SinkProvider, SinkRecipient {}
