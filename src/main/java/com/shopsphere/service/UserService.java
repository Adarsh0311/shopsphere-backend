package com.shopsphere.service;

import com.shopsphere.dto.JwtAuthResponse;
import com.shopsphere.dto.LoginRequest;
import com.shopsphere.dto.RegisterRequest;
import com.shopsphere.model.Role;
import com.shopsphere.model.User;
import com.shopsphere.repository.RoleRepository;
import com.shopsphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Registers a new user.
     * @param request The registration request DTO.
     * @return The registered User entity (or a UserResponse DTO if we create one).
     */
    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash the password
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());

        // Assign default role (e.g., ROLE_USER)
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Default user role not found!"));
        user.addRole(userRole);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public JwtAuthResponse login(LoginRequest loginRequest) {
        Authentication authenticate = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);

        // Generate JWT token
        UserDetails userDetails = (UserDetails) authenticate.getPrincipal();
        String jwt = jwtService.generateToken(userDetails); // Use username from authentication
        // Retrieve the full User object to get userId, email etc. for the response
        User user = findByUsername(authenticate.getName());


        return new JwtAuthResponse(
                jwt,
                "Bearer",
                user.getUserId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    /**
     * Finds a user by username. Used by Spring Security's UserDetailsService.
     * @param username The username to find.
     * @return The found User entity.
     */
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with username: " + username));
    }

    // We'll add login logic later in AuthController, after JWT utility.
    // For now, this service handles user persistence and retrieval.
}