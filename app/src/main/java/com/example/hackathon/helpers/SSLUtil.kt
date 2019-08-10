package com.example.hackathon.helpers


import org.spongycastle.jce.provider.BouncyCastleProvider
import org.spongycastle.openssl.PEMReader
import java.io.*
import java.nio.file.*
import java.security.*
import java.security.cert.*
import javax.net.ssl.*
import java.nio.file.Files.readAllBytes


object SSLUtil {
    @Throws(Exception::class)
    internal fun getSocketFactory(
        caCrtFile: String, crtFile: String, keyFile: String,
        password: String = ""
    ): SSLSocketFactory {
        Security.addProvider(BouncyCastleProvider())

        // load CA certificate
        var reader = PEMReader(InputStreamReader(ByteArrayInputStream(readAllBytes(Paths.get(caCrtFile)))))
        val caCert = reader.readObject() as X509Certificate
        reader.close()

        // load client certificate
        reader = PEMReader(InputStreamReader(ByteArrayInputStream(readAllBytes(Paths.get(crtFile)))))
        val cert = reader.readObject() as X509Certificate
        reader.close()

        // load client private key
//        reader = PEMReader(
//            InputStreamReader(ByteArrayInputStream(readAllBytes(Paths.get(keyFile)))),
//            PasswordFinder { password.toCharArray() }
//        )
        reader = PEMReader(InputStreamReader(ByteArrayInputStream(readAllBytes(Paths.get(keyFile)))))

        val key = reader.readObject() as KeyPair
        reader.close()

        // CA certificate is used to authenticate server
        val caKs = KeyStore.getInstance(KeyStore.getDefaultType())
        caKs.load(null, null)
        caKs.setCertificateEntry("ca-certificate", caCert)
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(caKs)

        // client key and certificates are sent to server so it can authenticate us
        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        ks.load(null, null)
        ks.setCertificateEntry("certificate", cert)
        ks.setKeyEntry(
            "private-key",
            key.private,
            password.toCharArray(),
            arrayOf<java.security.cert.Certificate>(cert)
        )
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(ks, password.toCharArray())

        // finally, create SSL socket factory
        val context = SSLContext.getInstance("TLSv1")
        context.init(kmf.keyManagers, tmf.trustManagers, null)

        return context.socketFactory
    }
}