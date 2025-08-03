/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.lang;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Objects;
import org.vesalainen.util.function.ByteConsumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class Bytes
{
    public static void set(boolean b, ByteConsumer consumer)
    {
        consumer.accept(b ? (byte)1 : (byte)0);
    }
    public static void set(byte b, ByteConsumer consumer)
    {
        consumer.accept(b);
    }
    public static void set(char v, ByteConsumer consumer)
    {
        consumer.accept((byte) ((v>>8)&0xff));
        consumer.accept((byte) (v&0xff));
    }
    public static void set(short v, ByteConsumer consumer)
    {
        consumer.accept((byte) ((v>>8)&0xff));
        consumer.accept((byte) (v&0xff));
    }
    public static void set(int v, ByteConsumer consumer)
    {
        consumer.accept((byte) ((v>>24)&0xff));
        consumer.accept((byte) ((v>>16)&0xff));
        consumer.accept((byte) ((v>>8)&0xff));
        consumer.accept((byte) (v&0xff));
    }
    public static void set(long v, ByteConsumer consumer)
    {
        consumer.accept((byte) ((v>>56)&0xff));
        consumer.accept((byte) ((v>>48)&0xff));
        consumer.accept((byte) ((v>>40)&0xff));
        consumer.accept((byte) ((v>>32)&0xff));
        consumer.accept((byte) ((v>>24)&0xff));
        consumer.accept((byte) ((v>>16)&0xff));
        consumer.accept((byte) ((v>>8)&0xff));
        consumer.accept((byte) (v&0xff));
    }
    public static void set(float v, ByteConsumer consumer)
    {
        set(Float.floatToRawIntBits(v), consumer);
    }
    public static void set(double v, ByteConsumer consumer)
    {
        set(Double.doubleToRawLongBits(v), consumer);
    }
    public static void set(Object v, ByteConsumer consumer)
    {
        if (v instanceof Serializable)
        {
            Serializable s = (Serializable) v;
            set(s, consumer);
        }
        else
        {
            byte[] array = Objects.toString(v).getBytes(UTF_8);
            int len = array.length;
            for (int ii=0;ii<len;ii++)
            {
                consumer.accept(array[ii]);
            }
        }
    }
    public static void set(Serializable v, ByteConsumer consumer)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos))
        {
            oos.writeObject(v);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        byte[] array = baos.toByteArray();
        int len = array.length;
        for (int ii=0;ii<len;ii++)
        {
            consumer.accept(array[ii]);
        }
    }
}
