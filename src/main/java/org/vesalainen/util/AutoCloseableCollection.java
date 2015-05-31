/*
 * Copyright (C) 2015 tkv
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
package org.vesalainen.util;

import java.io.IOException;
import java.util.Collection;

/**
 *
 * @author tkv
 * @param <T>
 */
public class AutoCloseableCollection<T extends AutoCloseable> implements AutoCloseable
{
    private final Collection<T> collection;

    public AutoCloseableCollection(Collection<T> collection)
    {
        this.collection = collection;
    }
    
    @Override
    public void close() throws IOException
    {
        for (T a : collection)
        {
            try
            {
                a.close();
            }
            catch (Exception ex)
            {
                throw new IOException(ex);
            }
        }
    }
    
}
