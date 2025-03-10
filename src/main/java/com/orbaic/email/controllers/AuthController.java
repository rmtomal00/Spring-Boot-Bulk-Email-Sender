package com.orbaic.email.controllers;

import com.orbaic.email.dto.LoginDto;
import com.orbaic.email.dto.ResetPassDto;
import com.orbaic.email.dto.UserDto;
import com.orbaic.email.responseModel.CustomResponse;
import com.orbaic.email.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    final CustomResponse customResponse;


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception exception) {
        System.err.println(exception);
        return customResponse.errorResponse(exception.getMessage(), 400);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto user, BindingResult bindingResult) throws Exception {
        if (Objects.isNull(user)){
            return customResponse.errorResponse("Body can't be null", 400);
        }
        if (bindingResult.hasErrors()) {
            return customResponse.errorResponse(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400);
        }
        authService.createUser(user);
        return customResponse.successWithoutDataRes("User registered successfully");
    }

    @GetMapping("/confirm-email/{token}")
    public ResponseEntity<?> confirmEmail(@PathVariable("token") String token) throws Exception {
        if (token == null) {
            return customResponse.errorResponse("token is missing", 400);
        }
        Map<String, Object> results = authService.confirmMail(token);
        return customResponse.successWithData(results);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPassDto reset, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            throw new Exception(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        if (Objects.isNull(reset)) {
            throw new Exception("Reset password body can't be null");
        }
        authService.resetPassword(reset);
        return customResponse.successWithoutDataRes("Reset password successful. Please check your email and confirm password email.");
    }

    @GetMapping("/reset-password-confirm/{token}")
    public ResponseEntity<?> resetPasswordConfirm(@PathVariable("token") String token) throws Exception {
        if (token == null) {
            throw new Exception("Reset password confirmation token can't be null");
        }
        token = token.trim();
        authService.confirmResetPassword(token);
        return customResponse.successWithoutDataRes("Reset password successfully completed");
    }
    @GetMapping("/resend-verification-email")
    public ResponseEntity<?> resendVerificationEmail(@Valid @RequestParam("email")  String email) throws Exception {
        if (email == null) {
            throw new Exception("Email can't be null");
        }
        email = email.trim();
        authService.resendEmail(email);
        return customResponse.successWithoutDataRes("We resend you a verification email. Please check your email and confirm your email.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto user, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            throw new Exception(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        Map<String, Object> results = authService.loginUser(user);
        return customResponse.successWithData(results);
    }

    @GetMapping("/token-check")
    public ResponseEntity<?> tokenCheck(@Valid @RequestParam("token") String token) throws Exception {
        if (token == null) {
            throw new Exception("Token can't be null");
        }
        Map<String, Object> results = authService.checkToken(token);
        return customResponse.successWithData(results);
    }
}
