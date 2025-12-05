package edu.dosw.rideci.application.port.in;

import edu.dosw.rideci.infrastructure.controllers.dto.Request.ForgotPasswordRequest;
import edu.dosw.rideci.infrastructure.controllers.dto.Request.ResetPasswordRequest;

public interface ResetPasswordUseCase {
    void requestPasswordReset(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
