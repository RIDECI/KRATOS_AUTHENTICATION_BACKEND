package edu.dosw.rideci.application.exceptions;

/**
 * Única excepción para todo el sistema de autenticación
 * Se usa para cualquier error: registro, login, tokens, etc.
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}