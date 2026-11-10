package com.nebula.app.util;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

/** Turns Firebase's technical errors into short messages a person can act on. */
public final class AuthErrors {
    private AuthErrors() {}

    public static final String INCORRECT = "Password or email is incorrect";

    public static String forLogin(Exception e) {
        if (e instanceof FirebaseNetworkException)
            return "No internet connection. Check your network and try again.";
        if (e instanceof FirebaseTooManyRequestsException)
            return "Too many attempts. Please wait a little and try again.";

        String msg = e == null || e.getMessage() == null ? "" : e.getMessage();
        // Firebase reports a wrong email/password as an "internal error" with one of these tokens inside
        if (msg.contains("INVALID_LOGIN_CREDENTIALS") || msg.contains("INVALID_PASSWORD")
                || msg.contains("EMAIL_NOT_FOUND") || msg.contains("INVALID_EMAIL")
                || msg.contains("credential is incorrect") || msg.contains("password is invalid"))
            return INCORRECT;

        if (e instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) e).getErrorCode();
            if ("ERROR_USER_DISABLED".equals(code)) return "This account has been disabled.";
            if ("ERROR_OPERATION_NOT_ALLOWED".equals(code))
                return "Email sign-in isn't turned on in Firebase yet.";
            // wrong password, unknown email, malformed email, expired credential, internal error...
            return INCORRECT;
        }
        return "Couldn't sign in. Please try again.";
    }

    public static String forRegister(Exception e) {
        if (e instanceof FirebaseNetworkException)
            return "No internet connection. Check your network and try again.";
        if (e instanceof FirebaseTooManyRequestsException)
            return "Too many attempts. Please wait a little and try again.";
        if (e instanceof FirebaseAuthUserCollisionException)
            return "An account with this email already exists.";
        if (e instanceof FirebaseAuthWeakPasswordException)
            return "Password is too weak. Use at least 6 characters.";
        if (e instanceof FirebaseAuthInvalidCredentialsException)
            return "Please enter a valid email address.";
        if (e instanceof FirebaseAuthException
                && "ERROR_OPERATION_NOT_ALLOWED".equals(((FirebaseAuthException) e).getErrorCode()))
            return "Email sign-in isn't turned on in Firebase yet.";
        return "Couldn't create the account. Please try again.";
    }
}
