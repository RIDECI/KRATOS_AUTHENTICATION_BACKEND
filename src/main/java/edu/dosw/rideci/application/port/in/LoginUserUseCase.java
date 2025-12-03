package edu.dosw.rideci.application.port.in;

import edu.dosw.rideci.infrastructure.controllers.dto.Request.LoginRequest;
import edu.dosw.rideci.infrastructure.controllers.dto.Response.AuthResponse;

public interface LoginUserUseCase {
    AuthResponse login(LoginRequest request);
}
