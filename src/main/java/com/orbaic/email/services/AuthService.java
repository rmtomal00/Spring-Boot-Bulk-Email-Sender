package com.orbaic.email.services;

import com.orbaic.email.config.MailConfig;
import com.orbaic.email.dto.LoginDto;
import com.orbaic.email.dto.ResetPassDto;
import com.orbaic.email.dto.UserDto;
import com.orbaic.email.enums.Role;
import com.orbaic.email.jwt.JwtManager;
import com.orbaic.email.models.auth.User;
import com.orbaic.email.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtManager jwtManager;
    private final MailConfig mailConfig;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    @Value("${spring.base.url}")
    String url;

    public void createUser( UserDto user) throws Exception {
        String userEmail =  user.getEmail().toLowerCase().trim();
        String hashPassword = bCryptPasswordEncoder.encode(user.getPassword());
        System.out.println("Hash Password: " + hashPassword);
        var u = userRepository.findByEmail(userEmail);

        if (u.isPresent()){
            System.out.println(u.get());
            throw new Exception("User already exists");
        }

        User users = User.builder()
                .email(user.getEmail().trim().toLowerCase())
                .password(hashPassword)
                .enabled(true)
                .isEmailVerified(false)
                .role(Role.USER.name())
                .username(user.getUsername())
                .build();

        users = userRepository.save(users);

        String token = jwtManager.generateToken(Map.of("userId", users.getId(), "email", users.getEmail()), users.getUsername(), System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        users.setTempToken(token);
        userRepository.save(users);
        String link = String.format("%s/api/v1/auth/confirm-email/%s", url, token);
        mailConfig.sendMail(users.getEmail(), "Varify Orbaic email service", String.format("<h3>Thank you.</h3><br> Please varify with this link <a href = '%s'> Link for confirm </a>", link) , true);
    }


    public Map<String, Object> confirmMail(@Valid String token) throws Exception {
        Map data = jwtManager.verify(token);
        String userEmail = (String) data.get("email");
        var findData = userRepository.findByEmail(userEmail);
        if (findData.isEmpty()){
            throw new Exception("User not found");
        }
        User user = findData.get();
        if (!user.getTempToken().equals(String.valueOf(token).trim())){
            throw new Exception("Token not Match");
        }
        if (user.getIsEmailVerified()){
            throw new Exception("Email already verified");
        }
        user.setIsEmailVerified(true);
        user.setTempToken(null);
        userRepository.save(user);
        return Map.of("status", "Email Verified");
    }

    public void resetPassword(@Valid ResetPassDto reset) throws Exception {
        String email = reset.getEmail().toLowerCase().trim();
        String hashPassword = bCryptPasswordEncoder.encode(reset.getPassword());
        var user = userRepository.findByEmail(email);
        if (user.isEmpty()){
            throw new Exception("User not found.");
        }
        User u = user.get();
        if (!u.getIsEmailVerified()){
            throw new Exception("Email not verified");
        }
        if (!u.isEnabled()){
            throw new Exception("Account not enabled. Please contact with support.");
        }

        String token = jwtManager.generateToken(Map.of("userId", u.getId(), "email", u.getEmail(), "hash", hashPassword), u.getUsername(), System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        u.setTempToken(token);
        userRepository.save(u);

        String link = String.format("%s/api/v1/auth/reset-password-confirm/%s", url, token);
        mailConfig.sendMail(u.getEmail(), "Reset Password", String.format("<h3>Thank you for stay with us.</h3><br> <p>Please verify you password reset with the link <br> <a href='%s'>Confirm Reset Password</a></p>", link) , true);
    }

    public void confirmResetPassword(String token) throws Exception {
        Map data = jwtManager.verify(token);
        String userEmail = (String) data.get("email");
        var findData = userRepository.findByEmail(userEmail);
        if (findData.isEmpty()){
            throw new Exception("User not found.");
        }
        User user = findData.get();
        if (!user.getIsEmailVerified()){
            throw new Exception("Email not verified");
        }
        if (!user.isEnabled()){
            throw new Exception("Account not enabled. Please contact with support.");
        }
        if (Objects.isNull(user.getTempToken())){
            throw new Exception("You have already reset your password");
        }
        user.setTempToken(null);
        user.setPassword((String) data.get("hash"));
        userRepository.save(user);
    }

    public void resendEmail(@Valid String email) throws Exception {
        if (email.isBlank()){
            throw new Exception("Invalid email address");
        }
        var dataU = userRepository.findByEmail(email);
        if (dataU.isEmpty()){
            throw new Exception("User not found.");
        }
        if (dataU.get().getIsEmailVerified()){
            throw new Exception("Email already verified");
        }
        if(!dataU.get().isEnabled()){
            throw new Exception("Account not enabled. Please contact with support.");
        }
        String token = jwtManager.generateToken(Map.of("userId", dataU.get().getId(), "email", dataU.get().getEmail()), dataU.get().getUsername(), System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        dataU.get().setTempToken(token);
        userRepository.save(dataU.get());
        String link = String.format("%s/api/v1/auth/confirm-email/%s", url, token);
        mailConfig.sendMail(dataU.get().getEmail(), "Varify Orbaic email service", String.format("<h3>Thank you.</h3><br> Please varify with this link <a href = '%s'> Link for confirm </a>", link) , true);
    }

    public Map<String, Object> loginUser(@Valid LoginDto user) throws Exception {
        String email = user.getEmail().toLowerCase().trim();
        var getUser = userRepository.findByEmail(email);
        if (getUser.isEmpty()){
            throw new Exception("User not found");
        }
        User u = getUser.get();
        if (!u.getIsEmailVerified()){
            throw new Exception("Email not verified");
        }
        if (!u.isEnabled()){
            throw new Exception("Account not enabled. Please contact with support.");
        }

        if (!bCryptPasswordEncoder.matches(user.getPassword(), u.getPassword())){
            throw new Exception("Passwords not match");
        }

        String token = jwtManager.generateToken(Map.of("id", u.getId(), "email", u.getEmail()), u.getUsername(), System.currentTimeMillis() + 24 * 60 * 60 * 1000);

        u.setToken(token);
        userRepository.save(u);
        return Map.of("accessToken", token, "userId", u.getId(), "message", "Login Successful and token will stay 24 hours");
    }

    public Map<String, Object> checkToken(@Valid String token) throws Exception {
        if (token.isBlank()){
            throw new Exception("Token can't be empty");
        }
        try {
            boolean isValid = !jwtManager.verify(token).isEmpty();
            return Map.of("valid", isValid, "message", "Token is valid");
        }catch (Exception e){
            return Map.of("valid", false, "message", e.getMessage());
        }
    }
}
