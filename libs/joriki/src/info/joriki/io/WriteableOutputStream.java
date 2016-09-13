/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.OutputStream;

/**
 * A writeable output stream is an output stream made to implement the
 * interface <code>Writeable</code>. All classes in this package that
 * extend plain output streams extend this class to allow these
 * streams to be used as writeables, and I recommend you do the same :-)
 */
public abstract class WriteableOutputStream extends OutputStream implements Writeable {}
