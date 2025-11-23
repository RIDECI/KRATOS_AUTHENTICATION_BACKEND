package edu.dosw.rideci.application.port.in;

import edu.dosw.rideci.infrastructure.controllers.dto.Response.UserResponse;
import edu.dosw.rideci.infrastructure.controllers.dto.Request.RegisterRequest;

public interface RegisterUserUseCase {
    UserResponse registerUser(RegisterRequest request);
}
