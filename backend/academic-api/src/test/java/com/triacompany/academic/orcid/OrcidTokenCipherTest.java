package com.triacompany.academic.orcid;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrcidTokenCipherTest {

    private final String encodedKey = Base64.getEncoder().encodeToString(
            "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)
    );

    @Test
    void shouldEncryptAndDecryptTokenWithAesGcm() {
        OrcidTokenCipher cipher = new OrcidTokenCipher(encodedKey);

        String encrypted = cipher.encrypt("orcid-access-token");

        assertTrue(cipher.isConfigured());
        assertTrue(encrypted.startsWith("v1:"));
        assertNotEquals("orcid-access-token", encrypted);
        assertEquals("orcid-access-token", cipher.decrypt(encrypted));
    }

    @Test
    void shouldUseRandomInitializationVector() {
        OrcidTokenCipher cipher = new OrcidTokenCipher(encodedKey);

        String first = cipher.encrypt("same-token");
        String second = cipher.encrypt("same-token");

        assertNotEquals(first, second);
        assertEquals("same-token", cipher.decrypt(first));
        assertEquals("same-token", cipher.decrypt(second));
    }
}
