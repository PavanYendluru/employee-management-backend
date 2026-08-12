package com.pavan.employeemanagement.security;

import com.pavan.employeemanagement.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Marks this class as a Spring Service Bean
@Service
public class JwtService {

    // Secret key used for signing the JWT
    private final byte[] secret;

    // Token expiration time in milliseconds
    private final long expiry;

    // Constructor Injection to load secret key and expiration time from application.properties
    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expiry) {

        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expiry = expiry;
    }

    // Creates a JWT token for the authenticated user
    public String create(User user) {

        try {

            // Calculate token expiration time
            long exp = Instant.now().toEpochMilli() + expiry;

            // Create JWT Header
            String header = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");

            // Create JWT Payload
            String payload = b64(
                    "{\"sub\":\"" + escape(user.getEmail()) +
                            "\",\"role\":\"" + user.getRole().name() +
                            "\",\"exp\":" + exp + "}"
            );

            // Generate the digital signature
            String signature = sign(header + "." + payload);

            // Return complete JWT token
            return header + "." + payload + "." + signature;

        } catch (Exception e) {

            // Throw exception if token creation fails
            throw new IllegalStateException("Cannot create token", e);
        }
    }

    // Extracts and validates the email (subject) from the JWT token
    public String subject(String token) {

        try {

            // Split the JWT into Header, Payload and Signature
            String[] parts = token.split("\\.");

            // Check whether token has exactly 3 parts
            if (parts.length != 3) {
                return null;
            }

            // Verify the digital signature
            if (!sign(parts[0] + "." + parts[1]).equals(parts[2])) {
                return null;
            }

            // Decode the payload
            String claims = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8
            );

            // Regular expression to extract email and expiration time
            Matcher matcher = Pattern.compile(
                    "\\\"sub\\\":\\\"([^\\\"]+)\\\".*\\\"exp\\\":(\\d+)"
            ).matcher(claims);

            // Return null if required fields are not found
            if (!matcher.find()) {
                return null;
            }

            // Read expiration time
            long expiration = Long.parseLong(matcher.group(2));

            // Check whether token has expired
            if (expiration <= Instant.now().toEpochMilli()) {
                return null;
            }

            // Return the email after unescaping special characters
            return matcher.group(1)
                    .replace("\\\\\"", "\"")
                    .replace("\\\\\\\\", "\\");

        } catch (Exception e) {

            // Return null if token is invalid
            return null;
        }
    }

    // Generates HMAC SHA-256 signature for the JWT
    private String sign(String value) throws Exception {

        // Create HMAC SHA-256 instance
        Mac mac = Mac.getInstance("HmacSHA256");

        // Initialize it with the secret key
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));

        // Generate and return Base64 URL encoded signature
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        mac.doFinal(value.getBytes(StandardCharsets.UTF_8))
                );
    }

    // Encodes a string into Base64 URL format without padding
    private String b64(String value) {

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        value.getBytes(StandardCharsets.UTF_8)
                );
    }

    // Escapes special characters before storing data inside JWT
    private String escape(String value) {

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}