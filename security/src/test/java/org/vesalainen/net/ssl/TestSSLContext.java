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

import org.vesalainen.security.cert.X509Generator;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 *
 * @author tkv
 */
public class TestSSLContext
{
    public static SSLContext getInstance() throws IOException
    {
        try
        {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            X509Generator gen = new X509Generator();
            X509Certificate cert = gen.generateSelfSignedCertificate("CN=localhost", keyPair, 10, "SHA256withRSA");
            SSLContext sslCtx = SSLContext.getInstance("TLSv1.2");
            sslCtx.init(new KeyManager[]{new KeyMan(cert, keyPair.getPrivate())}, new TrustManager[]{new TrustMan(cert)}, null);
            return sslCtx;
        }
        catch (NoSuchAlgorithmException | KeyManagementException | NoSuchProviderException | CertificateException ex)
        {
            throw new IOException(ex);
        }
    }
    
    private static class TrustMan extends X509ExtendedTrustManager
    {
        private X509Certificate cert;

        public TrustMan(X509Certificate cert)
        {
            this.cert = cert;
        }
        
        @Override
        public void checkClientTrusted(X509Certificate[] xcs, String string, Socket socket) throws CertificateException
        {
            check(xcs);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] xcs, String string, Socket socket) throws CertificateException
        {
            check(xcs);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException
        {
            check(xcs);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException
        {
            check(xcs);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException
        {
            check(xcs);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException
        {
            check(xcs);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private void check(X509Certificate[] xcs) throws CertificateException
        {
            if (xcs.length != 1)
            {
                throw new CertificateException("length != 1");
            }
            X509Certificate c = xcs[0];
            c.checkValidity();
            if (!cert.equals(c))
            {
                throw new CertificateException(c+" not match");
            }
        }
        
    }
    private static class KeyMan extends X509ExtendedKeyManager
    {
        private X509Certificate cert;
        private PrivateKey privateKey;

        public KeyMan(X509Certificate cert, PrivateKey privateKey)
        {
            this.cert = cert;
            this.privateKey = privateKey;
        }

        @Override
        public String[] getClientAliases(String string, Principal[] prncpls)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String chooseClientAlias(String[] strings, Principal[] prncpls, Socket socket)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String[] getServerAliases(String string, Principal[] prncpls)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String chooseServerAlias(String string, Principal[] prncpls, Socket socket)
        {
            return "alias";
        }

        @Override
        public X509Certificate[] getCertificateChain(String string)
        {
            return new X509Certificate[]{cert};
        }

        @Override
        public PrivateKey getPrivateKey(String string)
        {
            return privateKey;
        }

        @Override
        public String chooseEngineServerAlias(String string, Principal[] prncpls, SSLEngine ssle)
        {
            return "alias";
        }

        @Override
        public String chooseEngineClientAlias(String[] strings, Principal[] prncpls, SSLEngine ssle)
        {
            return "alias";
        }
        
    }
}
