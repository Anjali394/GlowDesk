package com.glowdesk.api.service;

import com.glowdesk.api.dto.request.LoginRequest;
import com.glowdesk.api.dto.request.RegisterRequest;
import com.glowdesk.api.dto.response.AuthResponse;
import com.glowdesk.api.entity.Customer;
import com.glowdesk.api.entity.Role;
import com.glowdesk.api.entity.User;
import com.glowdesk.api.exception.DuplicateResourceException;
import com.glowdesk.api.exception.ResourceNotFoundException;
import com.glowdesk.api.repository.CustomerRepository;
import com.glowdesk.api.repository.RoleRepository;
import com.glowdesk.api.repository.UserRepository;
import com.glowdesk.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER not found"));

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .roles(Set.of(customerRole))
                .build();
        userRepository.save(user);

        Customer customer = Customer.builder()
                .user(user)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .build();
        customerRepository.save(customer);

        String token = jwtUtil.generateToken(user.getEmail());
        return buildResponse(user, token, "Registered successfully. Please login to continue.");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is inactive");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return buildResponse(user, token, "Logged in successfully.");
    }

    private AuthResponse buildResponse(User user, String token, String message) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return new AuthResponse(message, token, user.getId(), user.getEmail(), roles);
    }
}