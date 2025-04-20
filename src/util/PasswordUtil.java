package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    
    private static final int SALT_LENGTH = 16;
    
    /**
     * Hash a password with SHA-256 and a random salt
     */
    public static String hashPassword(String plainPassword) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash the password with the salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(plainPassword.getBytes());
            
            // Combine salt and password for storage
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);
            
            return Base64.getEncoder().encodeToString(combined);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verify a password against a stored hash
     */
    public static boolean verifyPassword(String plainPassword, String storedPassword) {
        try {
            // Decode the stored password and salt
            byte[] combined = Base64.getDecoder().decode(storedPassword);
            
            // Extract the salt and hash
            byte[] salt = new byte[SALT_LENGTH];
            byte[] storedHash = new byte[combined.length - SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(combined, SALT_LENGTH, storedHash, 0, storedHash.length);
            
            // Hash the input password with the same salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] inputHash = md.digest(plainPassword.getBytes());
            
            // Compare the hashes
            if (inputHash.length != storedHash.length) {
                return false;
            }
            
            // Time-constant comparison to prevent timing attacks
            int diff = 0;
            for (int i = 0; i < inputHash.length; i++) {
                diff |= inputHash[i] ^ storedHash[i];
            }
            
            return diff == 0;
            
        } catch (Exception e) {
            return false;
        }
    }
}
