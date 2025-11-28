package edu.dosw.rideci.application.port.out;

import java.util.Date;

public interface TokenProviderOutPort {

    String generateAccessToken(String email, String role, Long userId);

    String generateRefreshToken(String email, Long userId);

    boolean isTokenValid(String token);

    boolean isAccessToken(String token);

    boolean isRefreshToken(String token);

    String getEmailFromToken(String token);

    Long getUserIdFromToken(String token);

    Date getExpirationFromToken(String token);
}