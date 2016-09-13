/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.DataOutput;

/*
 * This interface combines the functionality of {@link FullySeekable}
 * and {@link DataOutput}.
 */
public interface FullySeekableDataOutput extends FullySeekable,DataOutput {}
