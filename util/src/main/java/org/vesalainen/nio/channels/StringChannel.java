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
package org.vesalainen.nio.channels;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.vesalainen.nio.channels.SystemChannel.OutChannel;

/**
 * A utility.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class StringChannel extends OutChannel
{
    private final String charset;

    public StringChannel()
    {
        this(StandardCharsets.US_ASCII);
    }
    
    public StringChannel(Charset charset)
    {
        super(new ByteArrayOutputStream());
        this.charset = charset.name();
    }

    @Override
    public String toString()
    {
        try
        {
            ByteArrayOutputStream baos = (ByteArrayOutputStream) out;
            return baos.toString(charset);
        }
        catch (UnsupportedEncodingException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    
}
