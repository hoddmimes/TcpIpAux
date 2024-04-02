package com.hoddmimes.tcpip.test;


import com.hoddmimes.tcpip.crypto.DecryptInputStream;
import com.hoddmimes.tcpip.crypto.EncryptOutputStream;
import com.hoddmimes.tcpip.crypto.OpenSSHPublicKeyDecoder;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;

public class TestEncryption
{
    private static final String CIPHER_ALGO = "AES/CBC/PKCS5Padding";

    SecretKeySpec mSecretKey;


    public TestEncryption() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private Throwable initCrypto(boolean pEncrypt, SecretKeySpec pKey) {

        try {
            PipedInputStream tInStream = new PipedInputStream();
            PipedOutputStream tOutStream = new PipedOutputStream(tInStream);

            Cipher tCipher = Cipher.getInstance(CIPHER_ALGO, "BC");
            if (pEncrypt) {
                tCipher.init(Cipher.ENCRYPT_MODE, pKey, generateIV(pKey));
            } else {
                tCipher.init(Cipher.DECRYPT_MODE, pKey, generateIV(pKey));
            }



            if (pEncrypt) {
                EncryptOutputStream tEncrypt = new EncryptOutputStream(tOutStream, tCipher);
            } else {
                DecryptInputStream tDecrypt = new DecryptInputStream(tInStream, tCipher);
            }

        } catch (Throwable e) {
            return e;
        }
        return null;
    }

    private Object instanceOf( Class pClass ) {
        try {
            return pClass.getDeclaredConstructor().newInstance();
        }
        catch(Exception e) {
            return null;
        }
    }

    @Test
    public void testInitialization() {

        SecretKeySpec tSecretKey = generateKey(256);

        Assertions.assertNull(initCrypto(true, tSecretKey));
        Assertions.assertNull(initCrypto(false, tSecretKey));


        tSecretKey = generateKey(200);

        Assertions.assertEquals(initCrypto(true, tSecretKey).getClass(), InvalidAlgorithmParameterException.class);
        Assertions.assertEquals(initCrypto(false, tSecretKey).getClass(), InvalidAlgorithmParameterException.class);
    }

    private boolean compareByteArrays( byte[] a , byte[] b) {
        if (a == null && b == null) {
            return true;
        }
        if (a.length != b.length) {
            return false;
        }

        for( int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private SecretKeySpec generateKey( int tSizeBits ) {
        SecureRandom r = new SecureRandom();
        byte[] tKey;

        tKey = new byte[(tSizeBits/8)];
        r.nextBytes( tKey );
        return new SecretKeySpec( tKey,"AES");
    }

    private IvParameterSpec generateIV(SecretKeySpec tKey ) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return new IvParameterSpec(md.digest(tKey.getEncoded()));
        }
        catch( Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean encryptTestBufferSteps() throws Exception {
        byte xBuffer[];
        byte yBuffer[];


        SecretKeySpec tSecretKey = generateKey(256);

        Cipher tEncryptCipher = Cipher.getInstance(CIPHER_ALGO, "BC");
        Cipher tDecryptCipher = Cipher.getInstance(CIPHER_ALGO, "BC");

        tEncryptCipher.init(Cipher.ENCRYPT_MODE, tSecretKey, generateIV(tSecretKey));
        tDecryptCipher.init(Cipher.DECRYPT_MODE, tSecretKey, generateIV(tSecretKey));


        PipedInputStream  tInStream = new PipedInputStream( (1024 * 20) );
        PipedOutputStream tOutStream = new PipedOutputStream( tInStream );

       // tOutStream.write("hello".getBytes(StandardCharsets.UTF_8));
        // tOutStream.flush();

        //yBuffer = new byte[5];
        //tInStream.read(yBuffer);


        EncryptOutputStream tEncrypt = new EncryptOutputStream(tOutStream, tEncryptCipher);
        DecryptInputStream tDecrypt = new DecryptInputStream(tInStream, tDecryptCipher);


        SecureRandom r = new SecureRandom();
        for( int tSize = 1; tSize <= 1000; tSize++) {
            xBuffer = new byte[tSize];
            yBuffer = new byte[tSize];
            r.nextBytes( xBuffer );
            tEncrypt.write(xBuffer);
            tEncrypt.flush();


            tDecrypt.read(yBuffer);

            if (!compareByteArrays(xBuffer,yBuffer)) {
                return false;
            }
        }
        return true;
    }

    public Executable encryptTestRSAAuthorize() throws Exception
    {
        // Test that there is a public and private openssh RSA key file
        File tFile = new File("id_rsa_with_password_foobar");
        if (!tFile.canRead()) {
            throw new IOException("can not find or read file \"id_rsa_with_password_foobar\"");
        }
        tFile = new File("id_rsa_with_password_foobar.pub");
        if (!tFile.canRead()) {
            throw new IOException("can not find or read file \"id_rsa_with_password_foobar.pub\"");
        }
        String tAuthChallange = "buy the ticket and take the ride";

        PublicKey tPublicKey = loadPublicKey("id_rsa_with_password_foobar.pub");
        KeyPair tKeyPair = loadPrivateKey("id_rsa_with_password_foobar","foobar");


        // Encrypt the challange using the public key
        Cipher tInCipher = Cipher.getInstance("RSA", "BC");
        tInCipher.init(Cipher.ENCRYPT_MODE, tPublicKey);
        byte[] tEncryptedChallange = tInCipher.doFinal(tAuthChallange.getBytes(StandardCharsets.UTF_8));

        // Decrypt the challange using the private key
        Cipher tOutCipher = Cipher.getInstance("RSA", "BC");
        tOutCipher.init(Cipher.DECRYPT_MODE, tKeyPair.getPrivate());
        byte[] tDecryptedChallange = tOutCipher.doFinal(tEncryptedChallange);
        String tChallangeResponse = new String( tDecryptedChallange, StandardCharsets.UTF_8);
        if (!tChallangeResponse.contentEquals(tAuthChallange)) {
            throw new GeneralSecurityException("Authorize challange and authorize response is not same");
        }
        return null;
    }


    public Executable encryptTestECAuthorize() throws Exception
    {
        // Test that there is a public and private openssh ECDSA key file
        File tFile = new File("id_ecdsa_with_password_foobar");
        if (!tFile.canRead()) {
            throw new IOException("can not find or read file \"id_ecdsa_with_password_foobar\"");
        }
        tFile = new File("id_ecdsa_with_password_foobar.pub");
        if (!tFile.canRead()) {
            throw new IOException("can not find or read file \"id_ecdsa_with_password_foobar.pub\"");
        }
        String tAuthChallange = "buy the ticket and take the ride";

        PublicKey tPublicKey = loadPublicKey("id_ecdsa_with_password_foobar.pub");
        System.out.println(tPublicKey.getAlgorithm());
        KeyPair tKeyPair = loadPrivateKey("id_ecdsa_with_password_foobar","foobar");


        // Encrypt the challange using the public key
        Cipher tInCipher = Cipher.getInstance("ECIES", "BC");
        tInCipher.init(Cipher.ENCRYPT_MODE, tPublicKey);
        byte[] tEncryptedChallange = tInCipher.doFinal(tAuthChallange.getBytes(StandardCharsets.UTF_8));

        // Decrypt the challange using the private key
        Cipher tOutCipher = Cipher.getInstance("ECIES", "BC");
        tOutCipher.init(Cipher.DECRYPT_MODE, tKeyPair.getPrivate());
        byte[] tDecryptedChallange = tOutCipher.doFinal(tEncryptedChallange);
        String tChallangeResponse = new String( tDecryptedChallange, StandardCharsets.UTF_8);
        if (!tChallangeResponse.contentEquals(tAuthChallange)) {
            throw new GeneralSecurityException("Authorize challange and authorize response is not same");
        }
        return null;
    }

    private PublicKey loadPublicKey(String pPublicKeyFile) throws IOException, GeneralSecurityException
    {
        File tFile = new File( pPublicKeyFile );
        String tKeyString = new String( Files.readAllBytes( tFile.toPath()), StandardCharsets.UTF_8);

        OpenSSHPublicKeyDecoder tKeyDecoder = new OpenSSHPublicKeyDecoder();
        return tKeyDecoder.decodePublicKey( new String(Files.readAllBytes( tFile.toPath()), StandardCharsets.UTF_8));
    }


    private KeyPair loadPrivateKey( String pPrivateKeyFile, String pPassword ) throws IOException, GeneralSecurityException {
        File tFile = new File( pPrivateKeyFile );
        String tKeyString = new String( Files.readAllBytes( tFile.toPath()), StandardCharsets.UTF_8);

        PEMParser pemParser = new PEMParser(new StringReader(tKeyString));

        Object object = pemParser.readObject();
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        KeyPair tKeyPair;

        if (object instanceof PEMEncryptedKeyPair) {
            // Encrypted key - we will use provided password
            PEMEncryptedKeyPair ckp = (PEMEncryptedKeyPair) object;
            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(pPassword.toCharArray());
            tKeyPair = converter.getKeyPair(ckp.decryptKeyPair(decProv));
        } else {
            // Unencrypted key - no password needed
            PEMKeyPair ukp = (PEMKeyPair) object;
            tKeyPair = converter.getKeyPair(ukp);
        }

        return tKeyPair;
    }





    public boolean encryptTestRandom() throws Exception {
        SecretKeySpec tSecretKey = generateKey(256);


        PipedInputStream  tInStream = new PipedInputStream( (1024 * 20) );
        PipedOutputStream tOutStream = new PipedOutputStream( tInStream );

        Cipher tEncryptCipher = Cipher.getInstance(CIPHER_ALGO, "BC");
        Cipher tDecryptCipher = Cipher.getInstance(CIPHER_ALGO, "BC");


        tEncryptCipher.init(Cipher.ENCRYPT_MODE, tSecretKey, generateIV(tSecretKey));
        tDecryptCipher.init(Cipher.DECRYPT_MODE, tSecretKey, generateIV(tSecretKey));



        EncryptOutputStream tEncrypt = new EncryptOutputStream(tOutStream, tEncryptCipher);
        DecryptInputStream tDecrypt = new DecryptInputStream(tInStream, tDecryptCipher);

        SecureRandom r = new SecureRandom();
        byte xBuffer[];
        byte yBuffer[];

        for( int i = 0; i < 10000; i++) {
            int tSize = 1 + Math.abs(r.nextInt( (1024 * 16)));
            xBuffer = new byte[tSize];
            yBuffer = new byte[tSize];

            r.nextBytes( xBuffer );
            tEncrypt.write( xBuffer );
            tEncrypt.flush();



            tDecrypt.read(yBuffer);
            if (!compareByteArrays(xBuffer,yBuffer)) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void testEncryption() throws Exception {
        Assertions.assertEquals(encryptTestECAuthorize(),null);
        Assertions.assertEquals(encryptTestRSAAuthorize(),null);
        Assertions.assertTrue(encryptTestBufferSteps());
        Assertions.assertTrue(encryptTestRandom());

    }


}
