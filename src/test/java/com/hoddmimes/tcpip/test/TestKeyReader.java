package com.hoddmimes.tcpip.test;

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.Security;

public class TestKeyReader
{
    String mKeyFilename = null;
    String mPassword = null;

    public static void main(String[] args) {
        TestKeyReader t = new TestKeyReader();
        t.parseArguments(args);
        t.testFile();
    }

    public TestKeyReader() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private void testFile() {
        if (isPrivateKeyFile()) {
            testPrivateKeyFile();
        } else {
            testPublicKeyFile();
        }
    }

    private boolean isPrivateKeyFile() {
        try {
            Path tPath = Paths.get( mKeyFilename);
            String tContent = new String(Files.readAllBytes( tPath ), StandardCharsets.UTF_8);
            if (tContent.indexOf(" PRIVATE ") > 0) {
                return true;
            }
            return false;
        }
        catch( IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
        return false;
    }

    private void testPrivateKeyFile( ) {
        try {
            File tKeyFile = new File( mKeyFilename );
            String tKeyString = new String(Files.readAllBytes(tKeyFile.toPath()), StandardCharsets.UTF_8);

            PEMParser pemParser = new PEMParser(new StringReader(tKeyString));

            Object tKeyObject = pemParser.readObject();
            JcaPEMKeyConverter tConverter = new JcaPEMKeyConverter().setProvider("BC");
            KeyPair tKeyPair;

            if (tKeyObject instanceof PEMEncryptedKeyPair) {
                // Encrypted key - we will use provided password
                PEMEncryptedKeyPair ckp = (PEMEncryptedKeyPair) tKeyObject;
                PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(mPassword.toCharArray());
                tKeyPair = tConverter.getKeyPair(ckp.decryptKeyPair(decProv));
            } else {
                // Unencrypted key - no password needed
                PEMKeyPair ukp = (PEMKeyPair) tKeyObject;
                tKeyPair = tConverter.getKeyPair(ukp);
            }

            BCECPrivateKey pvk = (BCECPrivateKey) tKeyPair.getPrivate();
            System.out.println( tKeyPair.getPrivate().getAlgorithm());

        }
        catch( IOException e) {
            e.printStackTrace();
        }
    }

    private void testPublicKeyFile() {

    }


    private void parseArguments( String args[]) {
        int i = 0;
        while( i < args.length ) {
            if (args[i].contentEquals("-file")) {
                mKeyFilename = args[i+1];
                i++;
            }
            if (args[i].contentEquals("-password")) {
                mPassword = args[i+1];
                i++;
            }
            i++;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        if (mKeyFilename == null) {
            // Reading data using readLine
            while ((mKeyFilename == null) || (mKeyFilename.length() < 1)) {
                try {
                    System.out.print("ssh key file: ");
                    mKeyFilename = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        File tFile = new File( mKeyFilename);
        if (!tFile.canRead()) {
            System.out.println("can not find or read file \"" + mKeyFilename + "\"");
            System.exit(0);
        }

        if ((isPrivateKeyFile()) && (mPassword == null)) {
            System.out.print("ssh key file password: ");
            try {
                mPassword = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
