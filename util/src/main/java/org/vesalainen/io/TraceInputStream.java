/*
 * Copyright (C) 2016 tkv
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

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author tkv
 */
public class TraceInputStream extends InputStream
{
    private InputStream is;

    public TraceInputStream(InputStream is)
    {
        this.is = is;
    }
    
    @Override
    public int read() throws IOException
    {
        int cc = is.read();
        System.err.println(Thread.currentThread().getId()+" 0x"+Integer.toUnsignedString(cc, 16)+" '"+(char)cc+"'");
        return cc;
    }
    
}