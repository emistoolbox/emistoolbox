/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

/**
 * This is a marker interface used by the convenience methods in
 * {@link BitStreamCoders}. It is meant to be implemented by
 * bit stream decoders which work as converters, reading from a
 * {@link io.BitSource} and writing to a
 * {@link io.Writeable}.
 */
public interface BitStreamDecoder extends Crank {}
