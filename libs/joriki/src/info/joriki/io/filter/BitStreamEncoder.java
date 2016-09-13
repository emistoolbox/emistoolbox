/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

/**
 * This is a marker interface used by convenience methods in
 * {@link BitStreamCoders}. It is meant to be implemented by
 * bit stream encoders which work as output filters, providing a
 * {@link io.Writeable} as a data sink and writing to a
 * {@link io.BitSink}.
 */
public interface BitStreamEncoder extends OutputFilter {}
