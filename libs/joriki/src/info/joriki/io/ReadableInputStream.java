/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.InputStream;

/**
 * A readable input stream is an input stream made to implement the
 * interface <code>Readable</code>. All classes in this package that
 * extend plain input streams extend this class to allow these
 * streams to be used as readables, and I recommend you do the same :-)
 */
public abstract class ReadableInputStream extends InputStream implements Readable {}
