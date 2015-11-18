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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import org.vesalainen.util.BitArray;

/**
 * A class for compressing writing class public fields compressed. It is suitable
 * for fields that doesn't change much.
 * <p>Read compressed data with CompressedInput
 * @author tkv
 * @param <T> Object to be compressed
 * @see org.vesalainen.io.CompressedInput
 */
public class CompressedOutput<T> extends CompressedIO<T>
{
    private final byte[] buf2;
    private DataOutputStream dos;
    private OutputStream out;
    private ArrayOutputStream array;
    private DataOutputStream data;
    /**
     * Creates CompressedOutput
     * @param out 
     * @param obj
     * @throws IOException 
     */
    public CompressedOutput(OutputStream out, T obj) throws IOException
    {
        super(obj);
        this.out = out;
        
        fields = cls.getFields();

        for (Field field : fields)
        {
            Class<?> type = field.getType();
            bytes += getBytes(type.getName());
        }
        buf1 = new byte[bytes];
        buf2 = new byte[bytes];
        bitArray = new BitArray(bytes);
        bits = bitArray.getArray();
    }
    /**
     * Writes objects fields to stream
     * @return Compression rate
     * @throws IOException 
     */
    public float write() throws IOException
    {
        if (dos == null)
        {
            dos = new DataOutputStream(out);
            array = new ArrayOutputStream(buf1);
            data = new DataOutputStream(array);

            dos.writeUTF(cls.getName());
            dos.writeShort(fields.length);
            for (Field field : fields)
            {
                Class<?> type = field.getType();
                dos.writeUTF(field.getName());
                dos.writeUTF(type.getName());
            }
        }
        array.reset();
        for (Field field : fields)
        {
            write(data, field);
        }
        for (int ii=0;ii<bytes;ii++)
        {
            bitArray.set(ii, buf1[ii] != buf2[ii]);
        }
        dos.write(bits);
        int cnt = 0;
        for (int ii=0;ii<bytes;ii++)
        {
            if (bitArray.isSet(ii))
            {
                dos.write(buf1[ii]);
                buf2[ii] = buf1[ii];
                cnt++;
            }
        }
        return (float)cnt/(float)bytes;
    }

    private void write(DataOutputStream dos, Field field) throws IOException
    {
        try
        {
            switch (field.getType().getName())
            {
                case "boolean":
                    dos.writeBoolean(field.getBoolean(obj));
                    break;
                case "byte":
                    dos.writeByte(field.getByte(obj));
                    break;
                case "char":
                    dos.writeChar(field.getChar(obj));
                    break;
                case "short":
                    dos.writeShort(field.getShort(obj));
                    break;
                case "int":
                    dos.writeInt(field.getInt(obj));
                    break;
                case "float":
                    dos.writeFloat(field.getFloat(obj));
                    break;
                case "long":
                    dos.writeLong(field.getLong(obj));
                    break;
                case "double":
                    dos.writeDouble(field.getDouble(obj));
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
        out.close();
    }
    
    private class ArrayOutputStream extends OutputStream
    {
        private byte[] buffer;
        private int idx;

        public ArrayOutputStream(byte[] buffer)
        {
            this.buffer = buffer;
        }
        
        @Override
        public void write(int b) throws IOException
        {
            buffer[idx++] = (byte) b;
        }
        
        public void reset()
        {
            idx = 0;
        }
    }
}
