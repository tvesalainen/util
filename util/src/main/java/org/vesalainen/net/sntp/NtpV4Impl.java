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
package org.vesalainen.net.sntp;

import java.net.DatagramPacket;
import org.apache.commons.net.ntp.NtpV3Impl;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NtpV4Impl extends NtpV3Impl
{
    protected byte buf[];

    public NtpV4Impl()
    {
        setVersion(VERSION_4);
    }
    
    public void setReferenceId(ReferenceClock referenceClock)
    {
        setReferenceId(referenceClock.name());
    }

    public void setReferenceId(String referenceId)
    {
        int id = 0;
        for (int ii=0;ii<4;ii++)
        {
            id <<=8;
            if (referenceId.length() > ii)
            {
                id += referenceId.charAt(ii);
            }
        }
        setReferenceId(id);
    }

}
