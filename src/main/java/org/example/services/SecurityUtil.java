package org.example.services;

import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.BcryptFunction;

public class SecurityUtil {

    private static final BcryptFunction HASH_FUNCTION = BcryptFunction.getInstance(10);

    public static String hashPassword(String rawPassword) {
        Hash hash = Password.hash(rawPassword)
                .with(HASH_FUNCTION);
        return hash.getResult();
    }


    public static boolean verifyPassword(String rawPassword, String storedHash) {
        return Password.check(rawPassword, storedHash)
                .with(HASH_FUNCTION);
    }
}