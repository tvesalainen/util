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
package org.vesalainen.net.sntp;

import java.net.DatagramPacket;
import org.apache.commons.net.ntp.NtpV3Impl;

/**
 *
 * @author tkv
 */
public class NtpV4Impl extends NtpV3Impl
{
    protected byte buf[];

    public NtpV4Impl()
    {
        DatagramPacket datagramPacket = getDatagramPacket();
        buf = datagramPacket.getData(); // superclass buf is private
        setVersion(VERSION_4);
    }
    
    public void setReferenceId(ReferenceIdentifier referenceIdentifier)
    {
        int id = 0;
        String name = referenceIdentifier.name();
        for (int ii=0;ii<4;ii++)
        {
            id <<=8;
            if (name.length() > ii)
            {
                id += name.charAt(ii);
            }
        }
        setReferenceId(id);
    }

    public void setRootDelay(int value)
    {
        setInt(4, value);
    }
    
    public void setRootDispersion(int value)
    {
        setInt(8, value);
    }
    
    protected void setInt(int index, int value)
    {
        buf[index+3] = (byte) (value & 0xff);
        value >>=8;
        buf[index+2] = (byte) (value & 0xff);
        value >>=8;
        buf[index+1] = (byte) (value & 0xff);
        value >>=8;
        buf[index] = (byte) (value & 0xff);
    }
}
