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
package org.vesalainen.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import org.vesalainen.util.BitArray;

/**
 * A class for reading compressed data.
 * @author tkv
 */
public class CompressedInput<T> extends CompressedIO<T>
{
    private final InputStream in;
    private final DataInputStream dis;
    private final ArrayInputStream array;
    private final DataInputStream data;
    public CompressedInput(InputStream in, T obj) throws IOException
    {
        super(obj);
        
        this.in = in;
        this.dis = new DataInputStream(in);
        
        String classname = dis.readUTF();
        if (!cls.getName().equals(classname))
        {
            throw new IOException("Data input from "+classname+" not from "+cls);
        }
        short fieldCount = dis.readShort();
        Field[] flds = cls.getFields();
        if (fieldCount != flds.length)
        {
            throw new IOException("Field count "+fieldCount+" differs from "+fields.length);
        }
        fields = new Field[fieldCount];
        for (int ii=0;ii<fieldCount;ii++)
        {
            String fieldName = dis.readUTF();
            String fieldType = dis.readUTF();
            Field fld = null;
            for (Field f : flds)
            {
                if (f.getName().equals(fieldName))
                {
                    fld = f;
                    break;
                }
            }
            if (fld == null)
            {
                throw new IOException("Field "+fieldName+" not found ");
            }
            if (!fld.getType().getName().equals(fieldType))
            {
                throw new IOException("Field "+fieldName+" type differs "+fieldType+" <> "+fld);
            }
            fields[ii] = fld;
        }
        for (Field field : fields)
        {
            Class<?> type = field.getType();
            bytes += getBytes(type.getName());
        }
        buf1 = new byte[bytes];
        array = new ArrayInputStream(buf1);
        data = new DataInputStream(array);
        bitArray = new BitArray(bytes);
        bits = bitArray.getArray();
        
    }
    /**
     * Update objects fields. Throws EOFException when eof.
     * @return Compression rate
     * @throws IOException 
     */
    public float read() throws IOException
    {
        dis.readFully(bits);

        int cnt = 0;
        for (int ii=0;ii<bytes;ii++)
        {
            if (bitArray.isSet(ii))
            {
                buf1[ii] = dis.readByte();
                cnt++;
            }
        }
        array.reset();
        for (Field fld : fields)
        {
            read(data, fld);
        }
        return (float)cnt/(float)bytes;
    }
    
    private void read(DataInputStream ds, Field field) throws IOException
    {
        try
        {
            switch (field.getType().getName())
            {
                case "boolean":
                    field.setBoolean(obj, ds.readBoolean());
                    break;
                case "byte":
                    field.setByte(obj, ds.readByte());
                    break;
                case "char":
                    field.setChar(obj, ds.readChar());
                    break;
                case "short":
                    field.setShort(obj, ds.readShort());
                    break;
                case "int":
                    field.setInt(obj, ds.readInt());
                    break;
                case "float":
                    field.setFloat(obj, ds.readFloat());
                    break;
                case "long":
                    field.setLong(obj, ds.readLong());
                    break;
                case "double":
                    field.setDouble(obj, ds.readDouble());
                    break;
                default:
                    throw new IllegalArgumentException(field + " not allowed");
            }
        }
        catch (IllegalArgumentException | IllegalAccessException ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public void close() throws IOException
    {
        in.close();
    }
    
    private class ArrayInputStream extends InputStream
    {
        private final byte[] array;
        private int index;

        public ArrayInputStream(byte[] array)
        {
            this.array = array;
        }
        
        @Override
        public int read() throws IOException
        {
            return array[index++] & 0xff;
        }
        
        public void reset()
        {
            index = 0;
        }
    }
}
