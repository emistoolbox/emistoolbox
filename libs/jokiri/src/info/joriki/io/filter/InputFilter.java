/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

/**
   An input filter is similar to a filter input stream in the
   <code>java.io</code> package. It has an active input end
   and a passive output end, i.e. it reads and is read from.
*/
public interface InputFilter extends SourceProvider, SourceRecipient {}
