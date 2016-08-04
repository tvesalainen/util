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
package org.vesalainen.net.ssl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.net.ssl.HttpsURLConnection;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.junit.Test;
import org.vesalainen.util.HexDump;

/**
 *
 * @author tkv
 */
public class SSLT
{

    public SSLT()
    {
    }

    @Test
    public void test1() throws IOException
    {
        HttpsURLConnection con = (HttpsURLConnection) new URL("https://tvesalainen@github.com/tvesalainen/web-cache.git").openConnection();
        con.connect();
        System.err.println(con.getCipherSuite());
    }

    //@Test
    public void test2() throws IOException, CertificateException
    {
        String certs = "-----BEGIN CERTIFICATE-----"
                + "MIICiTCCAg+gAwIBAgIQH0evqmIAcFBUTAGem2OZKjAKBggqhkjOPQQDAzCBhTELMAkGA1UEBhMC"
                + "R0IxGzAZBgNVBAgTEkdyZWF0ZXIgTWFuY2hlc3RlcjEQMA4GA1UEBxMHU2FsZm9yZDEaMBgGA1UE"
                + "ChMRQ09NT0RPIENBIExpbWl0ZWQxKzApBgNVBAMTIkNPTU9ETyBFQ0MgQ2VydGlmaWNhdGlvbiBB"
                + "dXRob3JpdHkwHhcNMDgwMzA2MDAwMDAwWhcNMzgwMTE4MjM1OTU5WjCBhTELMAkGA1UEBhMCR0Ix"
                + "GzAZBgNVBAgTEkdyZWF0ZXIgTWFuY2hlc3RlcjEQMA4GA1UEBxMHU2FsZm9yZDEaMBgGA1UEChMR"
                + "Q09NT0RPIENBIExpbWl0ZWQxKzApBgNVBAMTIkNPTU9ETyBFQ0MgQ2VydGlmaWNhdGlvbiBBdXRo"
                + "b3JpdHkwdjAQBgcqhkjOPQIBBgUrgQQAIgNiAAQDR3svdcmCFYX7deSRFtSrYpn1PlILBs5BAH+X"
                + "4QokPB0BBO490o0JlwzgdeT6+3eKKvUDYEs2ixYjFq0JcfRK9ChQtP6IHG4/bC8vCVlbpVsLM5ni"
                + "wz2J+Wos77LTBumjQjBAMB0GA1UdDgQWBBR1cacZSBm8nZ3qQUfflMRId5nTeTAOBgNVHQ8BAf8E"
                + "BAMCAQYwDwYDVR0TAQH/BAUwAwEB/zAKBggqhkjOPQQDAwNoADBlAjEA7wNbeqy3eApyt4jf/7VG"
                + "FAkK+qDmfQjGGoe9GKhzvSbKYAydzpmfz1wPMOG+FDHqAjAU9JM8SaczepBGR7NjfRObTrdvGDeA"
                + "U/7dIOA1mjbRxwG55tzd8/8dLDoWV9mSOdY="
                + "-----END CERTIFICATE-----";
        ByteArrayInputStream bais = new ByteArrayInputStream(certs.getBytes());
        BufferedInputStream bis = new BufferedInputStream(bais);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        while (bis.available() > 0)
        {
            Certificate cert = cf.generateCertificate(bis);
            System.out.println(cert.toString());
        }
    }

    //@Test
    public void test3() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException
    {
        char[] pwd = "changeit".toCharArray();
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(new File("C:\\Program Files\\Java\\jdk1.8.0_77\\jre\\lib\\security\\cacerts")), pwd);
        Certificate certificate = keyStore.getCertificate("verisignclass1g2ca");
        System.err.println(certificate);
    }

    @Test
    public void test4() throws IOException
    {
        sun.security.x509.X500Name sunName = new sun.security.x509.X500Name("CN=timo, C=FI");
        org.bouncycastle.asn1.x500.X500Name bcName = new org.bouncycastle.asn1.x500.X500Name(RFC4519Style.INSTANCE, "CN=timo, C=FI");
        byte[] sunEncoded = sunName.getEncoded();
        byte[] bcEncoded = bcName.getEncoded();
        System.err.println(HexDump.toHex(sunEncoded));
        System.err.println(HexDump.toHex(bcEncoded));
        //assertArrayEquals(sunEncoded, bcEncoded); // bc uses utf8string for cn sun uses printablestring for both
    }

}
