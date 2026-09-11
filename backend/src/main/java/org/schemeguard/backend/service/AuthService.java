package org.schemeguard.backend.service;

import lombok.RequiredArgsConstructor;
import org.schemeguard.backend.dto.AccountManagement.AuthResponse;
import org.schemeguard.backend.dto.AccountManagement.LoginRequest;
import org.schemeguard.backend.dto.AccountManagement.RegisterRequest;
import org.schemeguard.backend.dto.AccountManagement.UpdateProfileRequest;
import org.schemeguard.backend.entity.Role;
import org.schemeguard.backend.entity.User;
import org.schemeguard.backend.exception.ConflictException;
import org.schemeguard.backend.exception.UnauthorizedException;
import org.schemeguard.backend.repository.RolesRepository;
import org.schemeguard.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException(
                    "Email is already registered"
            );
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setFullName(request.fullName().trim());
        user.setStatus("ACTIVE");

        Role merchantRole =
                rolesRepository.findRoleByName("MERCHANT");

        if (merchantRole != null) {
            user.getRoles().add(merchantRole);
        }

        User savedUser = userRepository.save(user);

        return toAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email()
                .trim()
                .toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Invalid email or password"
                        )
                );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new UnauthorizedException(
                    "Invalid email or password"
            );
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new UnauthorizedException(
                    "User account is inactive"
            );
        }

        return toAuthResponse(user);
    }

    @Transactional
    public AuthResponse updateProfile(User user, UpdateProfileRequest request) {
        String email = request.email().trim().toLowerCase();

        userRepository.findByEmailIgnoreCase(email)
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {
                    throw new ConflictException("Email is already registered");
                });

        user.setEmail(email);
        user.setFullName(request.fullName().trim());

        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        return toAuthResponse(userRepository.save(user));
    }

    private AuthResponse toAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user);

        return new AuthResponse(
                accessToken,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus()
        );
    }
}
