/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

/**
 * This interface combines the extensions that <code>RelativelySeekable</code>
 * and <code>TerminallySeekable</code> make to the <code>Seekable</code>
 * interface. Thus it provides all three modes of seeking available in C:
 * relative to beginning, current position or end.
 */
public interface FullySeekable extends RelativelySeekable,TerminallySeekable {}
