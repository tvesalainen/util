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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class NtpV4ImplTest
{
    
    public NtpV4ImplTest()
    {
    }

    @Test
    public void test1()
    {
        NtpV4Impl ntp = new NtpV4Impl();
        ntp.setReferenceId(ReferenceIdentifier.GPS);
        String referenceIdString = ntp.getReferenceIdString();
        assertEquals("GPS", referenceIdString);
        
        ntp.setRootDelay(12345);
        assertEquals(12345, ntp.getRootDelay());
        
        ntp.setRootDispersion(98765);
        assertEquals(98765, ntp.getRootDispersion());
    }
    
}
