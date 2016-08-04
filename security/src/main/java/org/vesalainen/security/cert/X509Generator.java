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
package org.vesalainen.security.cert;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.vesalainen.lang.Primitives;

/**
 *
 * @author tkv
 */
public class X509Generator
{

    /**
     * Create a self-signed X.509 Certificate
     *
     * @param subjectDN the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
     * @param pair the KeyPair
     * @param days how many days from now the Certificate is valid for
     * @param algorithm the signing algorithm, e.g. "SHA1withRSA"
     * @return 
     * @throws java.security.cert.CertificateException 
     */
    public X509Certificate generateSelfSignedCertificate(String subjectDN, KeyPair pair, int days, String algorithm) throws CertificateException
    {
        return generateCertificate(subjectDN, null, pair, null, days, algorithm);
    }
    /**
     * Create a signed X.509 Certificate
     * @param subjectDN the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
     * @param issuerDN Signers X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
     * @param pair the KeyPair
     * @param privkey Signers private key
     * @param days how many days from now the Certificate is valid for
     * @param signingAlgorithm the signing algorithm, e.g. "SHA1withRSA"
     * @return 
     * @throws java.security.cert.CertificateException 
     */
    public X509Certificate generateCertificate(String subjectDN, String issuerDN, KeyPair pair, PrivateKey privkey, int days, String signingAlgorithm) throws CertificateException
    {
        if (privkey == null)
        {
            privkey = pair.getPrivate();
        }
        X500Name issuer;
        if (issuerDN == null)
        {
            issuer = new X500Name(RFC4519Style.INSTANCE, subjectDN);
        }
        else
        {
            issuer = new X500Name(RFC4519Style.INSTANCE, issuerDN);
        }
        long now = System.currentTimeMillis();
        BigInteger serial = BigInteger.probablePrime(64, new SecureRandom(Primitives.writeLong(now)));
        X500Name subject = new X500Name(RFC4519Style.INSTANCE, subjectDN);
        PublicKey publicKey = pair.getPublic();
        byte[] encoded = publicKey.getEncoded();
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(encoded);
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                issuer,
                serial,
                new Date(now - 86400000l),
                new Date(now + days * 86400000l),
                subject,
                subjectPublicKeyInfo
        );
        X509CertificateHolder holder = builder.build(createSigner(privkey, signingAlgorithm));
        return new JcaX509CertificateConverter().getCertificate(holder);
    }

    public static ContentSigner createSigner(PrivateKey privateKey, String signingAlgorithm)
    {
        try
        {
            AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(signingAlgorithm);
            AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

            return new BcRSAContentSignerBuilder(sigAlgId, digAlgId)
                    .build(PrivateKeyFactory.createKey(privateKey.getEncoded()));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not create content signer.", e);
        }
    }
}
