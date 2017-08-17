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
package org.vesalainen.security.cert;

import org.vesalainen.security.cert.X509Generator;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import org.junit.Test;
import org.vesalainen.security.cert.X509Generator;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class X509GenTest
{
    
    public X509GenTest()
    {
    }

    @Test
    public void testit() throws NoSuchAlgorithmException, GeneralSecurityException, IOException
    {
        X509Generator gen = new X509Generator();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        KeyPair ssKeyPair = kpg.generateKeyPair();
        X509Certificate ssCert = gen.generateCertificate("CN=timo, C=FI", null, ssKeyPair, null, 1000, "SHA1withRSA");
        System.err.println(ssCert);
        KeyPair keyPair = kpg.generateKeyPair();
        X509Certificate cert = gen.generateCertificate("CN=uhri", "CN=timo, C=FI", keyPair, ssKeyPair.getPrivate(), 1000, "SHA1withRSA");
        System.err.println(cert);
    }
    
}
