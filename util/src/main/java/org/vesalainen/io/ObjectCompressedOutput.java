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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.util.BitArray;

/**
 * A class for compressing writing class public fields compressed. It is suitable
 * for fields that doesn't change much.
 * <p>Read compressed data with CompressedInput
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T> Object to be compressed
 * @see org.vesalainen.io.CompressedInput
 */
public class ObjectCompressedOutput<T> extends CompressedOutput
{
    private T obj;
    private Class<T> cls;
    private final Field[] fields;
    /**
     * Creates CompressedOutput
     * @param out 
     * @param obj
     * @throws IOException 
     */
    public ObjectCompressedOutput(OutputStream out, T obj) throws IOException
    {
        super(out, obj.getClass().getName());
        this.obj = obj;
        this.cls = (Class<T>) obj.getClass();
        
        fields = cls.getFields();

        for (Field field : fields)
        {
            Class<?> type = field.getType();
            add(field.getName(), type.getSimpleName(), true);
        }
        ready();
    }

    @Override
    public float write() throws IOException
    {
        try
        {
            for (Field field : fields)
            {
                set(field.getName(), field.get(obj));
            }
        }
        catch (IllegalArgumentException | IllegalAccessException ex)
        {
            throw new IOException(ex);
        }
        return super.write();
    }
    
}
