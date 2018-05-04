/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Spliterators.AbstractSpliterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.vesalainen.util.BitArray;

/**
 * A class for reading compressed data.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class ObjectCompressedInput<T> extends CompressedInput
{
    private T obj;
    private Class<T> cls;
    private final Field[] fields;
    
    public ObjectCompressedInput(InputStream in, T obj) throws IOException
    {
        super(in, obj != null ? obj.getClass().getName() : null);
        this.obj = obj;
        if (obj == null)
        {
            try
            {
                cls = (Class<T>) Class.forName(source);
                obj = cls.newInstance();
            }
            catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
            {
                throw new IOException(ex);
            }
        }
        else
        {
            cls = (Class<T>) obj.getClass();
        }
        fields = cls.getFields();
        int fieldCount = getPropertyCount();
        if (fieldCount != fields.length)
        {
            throw new IOException("Field count "+fieldCount+" differs from "+fields.length);
        }
    }

    @Override
    public float read() throws IOException
    {
        float ratio = super.read();
        if (ratio < 0)
        {
            throw new EOFException();
        }
        try
        {
            for (Field field : fields)
            {
                Object value = get(field.getName());
                field.set(obj, value);
            }
        }
        catch (IllegalArgumentException | IllegalAccessException ex)
        {
            throw new IOException(ex);
        }
        return ratio;
    }
    /**
     * Return a stream for compressed input.
     * <p>Note that streamed objects are always the same as given in 
     * constructor!
     * @return 
     */
    public Stream<T> stream()
    {
        return StreamSupport.stream(new SpliteratorImpl(), false);
    }
    private class SpliteratorImpl extends AbstractSpliterator<T>
    {

        public SpliteratorImpl()
        {
            super(Long.MAX_VALUE, 0);
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action)
        {
            try
            {
                read();
                action.accept(obj);
                return true;
            }
            catch (EOFException ex)
            {
                return false;
            }
            catch (IOException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        
    }
}
