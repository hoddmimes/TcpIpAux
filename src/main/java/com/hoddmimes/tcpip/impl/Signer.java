package com.hoddmimes.tcpip.impl;

import com.hoddmimes.tcpip.crypto.OpenSSHPublicKeyDecoder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import javax.crypto.Cipher;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Signer
{
    private HashMap<String, PublicKeyFile> mServerPublicKeys = null;
    private String mServerPublicKeyPath = null;
    private KeyPair             mClientPrivateKeyPair = null;


    public Signer(String pPublicKeysPath ) {
        if (pPublicKeysPath != null) {
            Security.addProvider(new BouncyCastleProvider());
            mServerPublicKeyPath = pPublicKeysPath;
            loadPublicKeys();
        }
    }

    public Signer(String pClientPrivateKeyFilename, String pPassword ) {
        if (pClientPrivateKeyFilename != null) {
            Security.addProvider(new BouncyCastleProvider());
            try {
                mClientPrivateKeyPair = loadPrivateKey(pClientPrivateKeyFilename, pPassword);
            }
            catch( Exception e) {
                e.printStackTrace();
            }
        }
    }


    /*
        This method is invoked by the server when to encrypt a challange that is to be sent to the connecting client.
        The challange is encrypted with the client public key and can only be decrypted with the corresponding client private key
     */
    public byte[] serverEncryptChallance( byte[] pChallange, PublicKey pClientPublicKey ) throws GeneralSecurityException
    {
        if (pClientPublicKey.getAlgorithm().contentEquals("RSA")) {
            Cipher tInCipher = Cipher.getInstance("RSA", "BC");
            tInCipher.init(Cipher.ENCRYPT_MODE, pClientPublicKey);
            return tInCipher.doFinal(pChallange);
        } if (pClientPublicKey.getAlgorithm().contentEquals("ECDSA")) {
            Cipher tInCipher = Cipher.getInstance("ECIES", "BC");
            tInCipher.init(Cipher.ENCRYPT_MODE, pClientPublicKey);
            return tInCipher.doFinal(pChallange);
        }
        throw new GeneralSecurityException("Can not encrypt challange, only RSA and EC keys are supported");

    }



    public byte[] clientDecryptChallance( byte[] pChallange, PublicKey pClientPublicKey ) throws GeneralSecurityException
    {
        if (pClientPublicKey.getAlgorithm().contentEquals("RSA")) {
            Cipher tInCipher = Cipher.getInstance("RSA", "BC");
            tInCipher.init(Cipher.ENCRYPT_MODE, pClientPublicKey);
            return tInCipher.doFinal(pChallange);
        } if (pClientPublicKey.getAlgorithm().contentEquals("ECDSA")) {
        Cipher tInCipher = Cipher.getInstance("ECIES", "BC");
        tInCipher.init(Cipher.ENCRYPT_MODE, pClientPublicKey);
        return tInCipher.doFinal(pChallange);
    }
        throw new GeneralSecurityException("Can not encrypt challange, only RSA and EC keys are supported");

    }

    public PublicKey serverGetClientPublicKey( byte[] pEncodedPublicSSHKey ) {
       for( PublicKeyFile pkf : this.mServerPublicKeys.values() ) {
           if (pkf.isEqual(pEncodedPublicSSHKey)) {
                return pkf.getPublicKey();
           }
       }
        // Not found among the keys being loadded so far, try to see if any new keys has been uploaded
        if (this.loadPublicKeys()) {
            for( PublicKeyFile pkf : this.mServerPublicKeys.values() ) {
                if (pkf.isEqual(pEncodedPublicSSHKey)) {
                    return pkf.getPublicKey();
                }
            }
        }
        return null;
    }

    public byte[] clientGetPublicKeyEncoded() throws IOException {
        if (this.mClientPrivateKeyPair == null) {
            throw new IOException("Invalid Authorizer, context is not a client authorizer");
        }
        return this.mClientPrivateKeyPair.getPublic().getEncoded();
    }

    public byte[] clientSignMessage(byte[] pMessage) throws GeneralSecurityException{
        if (this.mClientPrivateKeyPair == null) {
            throw new GeneralSecurityException("Client signing is required, but no client private key has been provided");
        }
        if (this.mClientPrivateKeyPair.getPrivate().getAlgorithm().contentEquals("RSA")) {
            Signature rsa = Signature.getInstance("SHA256withRSA");
            rsa.initSign(this.mClientPrivateKeyPair.getPrivate());
            rsa.update(pMessage);
            return rsa.sign();
        } else if (this.mClientPrivateKeyPair.getPrivate().getAlgorithm().contentEquals( "DSA")) {
            Signature dsa = Signature.getInstance("SHA256withDSA");
            dsa.initSign(this.mClientPrivateKeyPair.getPrivate());
            dsa.update(pMessage);
            return dsa.sign();
        }
        else if (this.mClientPrivateKeyPair.getPrivate().getAlgorithm().contentEquals("ECDSA")) {
            Signature ecdsa = Signature.getInstance("SHA256withECDSA");
            ecdsa.initSign(this.mClientPrivateKeyPair.getPrivate());
            ecdsa.update(pMessage);
            return ecdsa.sign();
        }
        throw new GeneralSecurityException("Unsupported Signature private key");
    }

    public boolean serverVerifyMessage(byte[] pMessage, PublicKey pPublicKey, byte[] pSignaturePayload) throws VerifyError {
        try {
            if (pPublicKey.getAlgorithm().contentEquals("RSA")) {
                Signature ecdsa = Signature.getInstance("SHA256withRSA");
                ecdsa.initVerify(pPublicKey);
                ecdsa.update(pMessage);
                return ecdsa.verify(pSignaturePayload);
            } else if (pPublicKey.getAlgorithm().contentEquals("DSA")) {
                Signature dsa = Signature.getInstance("SHA256withDSA");
                dsa.initVerify(pPublicKey);
                dsa.update(pMessage);
                return dsa.verify(pSignaturePayload);
            } else if (pPublicKey.getAlgorithm().contentEquals("ECDSA")) {
                Signature ecdsa = Signature.getInstance("SHA256withECDSA");
                ecdsa.initVerify(pPublicKey);
                ecdsa.update(pMessage);
                return ecdsa.verify(pSignaturePayload);
            }
        }
        catch( GeneralSecurityException e) {
            throw new VerifyError("Sign verifivcation failure, reason: " + e.getMessage());
        }
        return false;
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
    private boolean loadPublicKeys() {
        boolean tNewKeys = false;

        if (mServerPublicKeys == null) {
            mServerPublicKeys = new HashMap<>();
        }

        Set<String> tFiles = listPubKeyFiles(this.mServerPublicKeyPath);
        for( String tFilename : tFiles) {
            try {
                if (!mServerPublicKeys.containsKey( tFilename )) {
                    tNewKeys = true;
                    mServerPublicKeys.put(tFilename, new PublicKeyFile(tFilename));
                }
            }
            catch( Exception e) {
                e.printStackTrace();
            }
        }
        return tNewKeys;
    }

    private Set<String> listPubKeyFiles(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> (!file.isDirectory()) && (file.getName().endsWith(".pub")))
                .map(File::getAbsolutePath)
                .collect(Collectors.toSet());
    }


    class PublicKeyFile {
        private String mKeyFilename;
        private PublicKey mPublicKey;
        private byte[] mEncodedKey;

        PublicKeyFile( String pKeyFilename ) throws IOException, GeneralSecurityException {
            mKeyFilename = pKeyFilename;
            File tKeyFile = new File( pKeyFilename );
            OpenSSHPublicKeyDecoder tDecoder = new OpenSSHPublicKeyDecoder();
            mPublicKey = tDecoder.decodePublicKey( tKeyFile );
            mEncodedKey = mPublicKey.getEncoded();
        }


        byte[] getEncodedKey() {
            return mEncodedKey;
        }

        boolean isEqual( byte[] pEncodedKey) {
            return Arrays.equals( mEncodedKey, pEncodedKey);
        }

        PublicKey getPublicKey() {
            return mPublicKey;
        }

        String getFilename() {
            return mKeyFilename;
        }
    }
}
