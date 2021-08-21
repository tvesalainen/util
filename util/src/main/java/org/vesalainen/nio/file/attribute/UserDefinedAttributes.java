/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nio.file.attribute;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface UserDefinedAttributes
{

    boolean arraysEquals(String name, byte[] array) throws IOException;

    boolean arraysEquals(String name, byte[] array, int offset, int length) throws IOException;

    void delete(String name) throws IOException;

    void deleteAll() throws IOException;

    byte[] get(String name) throws IOException;

    boolean getBoolean(String name) throws IOException;

    double getDouble(String name) throws IOException;

    int getInt(String name) throws IOException;

    long getLong(String name) throws IOException;

    String getString(String name) throws IOException;

    boolean has(String name) throws IOException;

    Collection<String> list() throws IOException;

    int read(String name, ByteBuffer dst) throws IOException;

    void set(String name, byte[] array) throws IOException;

    void set(String name, byte[] array, int offset, int length) throws IOException;

    void setBoolean(String name, boolean value) throws IOException;

    void setDouble(String name, double value) throws IOException;

    void setInt(String name, int value) throws IOException;

    void setLong(String name, long value) throws IOException;

    void setString(String name, String value) throws IOException;

    int size(String name) throws IOException;

    int write(String name, ByteBuffer src) throws IOException;
    
}
