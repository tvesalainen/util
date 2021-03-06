/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.nio;

import java.io.IOException;
import java.nio.Buffer;

/**
 * A FunctionalInterface for Scattering/Gathering operations.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <B>
 */
@FunctionalInterface
public interface SparseBufferOperator<B extends Buffer>
{
    long apply(B[] bbs, int offset, int length) throws IOException;
    default long apply(B[] bbs) throws IOException
    {
        return apply(bbs, 0, bbs.length);
    }
}
