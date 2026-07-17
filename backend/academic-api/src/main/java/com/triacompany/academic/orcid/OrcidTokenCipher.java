package com.triacompany.academic.orcid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class OrcidTokenCipher {

    private static final String VERSION = "v1";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final String encodedKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public OrcidTokenCipher(
            @Value("${app.orcid.oauth.token-encryption-key:}") String encodedKey
    ) {
        this.encodedKey = encodedKey == null ? "" : encodedKey.trim();
    }

    public boolean isConfigured() {
        try {
            return keyBytes().length == 32;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(keyBytes(), "AES"),
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            );

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] payload = ByteBuffer.allocate(iv.length + encrypted.length)
                    .put(iv)
                    .put(encrypted)
                    .array();

            return VERSION + ":" + Base64.getUrlEncoder().withoutPadding().encodeToString(payload);
        } catch (GeneralSecurityException exception) {
            throw new OrcidOAuthException(
                    "token_encryption_failed",
                    "Não foi possível proteger o token ORCID.",
                    exception
            );
        }
    }

    public String decrypt(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            return null;
        }

        String prefix = VERSION + ":";
        if (!encryptedValue.startsWith(prefix)) {
            throw new OrcidOAuthException(
                    "token_format_invalid",
                    "O token ORCID armazenado possui formato inválido."
            );
        }

        try {
            byte[] payload = Base64.getUrlDecoder().decode(encryptedValue.substring(prefix.length()));
            if (payload.length <= IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Payload cifrado incompleto.");
            }

            byte[] iv = new byte[IV_LENGTH_BYTES];
            byte[] encrypted = new byte[payload.length - IV_LENGTH_BYTES];
            System.arraycopy(payload, 0, iv, 0, IV_LENGTH_BYTES);
            System.arraycopy(payload, IV_LENGTH_BYTES, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(keyBytes(), "AES"),
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            );

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new OrcidOAuthException(
                    "token_decryption_failed",
                    "Não foi possível recuperar o token ORCID armazenado.",
                    exception
            );
        }
    }

    private byte[] keyBytes() {
        if (encodedKey.isBlank()) {
            throw new IllegalStateException("Chave de criptografia ORCID não configurada.");
        }

        byte[] decoded = Base64.getDecoder().decode(encodedKey);
        if (decoded.length != 32) {
            throw new IllegalStateException(
                    "A chave de criptografia ORCID deve possuir exatamente 32 bytes em Base64."
            );
        }

        return decoded;
    }
}
