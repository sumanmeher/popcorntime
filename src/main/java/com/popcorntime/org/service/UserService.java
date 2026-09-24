package com.popcorntime.org.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.popcorntime.org.dto.RegisterRequest;
import com.popcorntime.org.dto.Role;
import com.popcorntime.org.entity.AppUser;
import com.popcorntime.org.entity.UserRole;
import com.popcorntime.org.exception.BadRequestException;
import com.popcorntime.org.exception.NotFoundException;
import com.popcorntime.org.repository.RoleRepository;
import com.popcorntime.org.repository.UserRepository;

@Service
/**
 * Service class responsible for handling business logic related to users.
 * This includes user registration, validation, and role assignment.
 */
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registers a new user in the system.
     * Validates that the email is unique and that the requested role is USER.
     * Encrypts the password before saving the user to the database.
     *
     * @param registerRequest The DTO containing user registration details (name, email, password, etc.).
     * @return A success message upon successful registration.
     * @throws BadRequestException If a user with the given email already exists, or if a non-USER role is requested.
     * @throws NotFoundException If the USER role is not found in the database.
     */
    public String registerUser(RegisterRequest registerRequest) {

        // check if user already exists with email
        Optional<AppUser> user = userRepository.findByEmail(registerRequest.getEmail());
        if (user.isPresent()) {
            throw new BadRequestException("User already exists with email");
        }

        // Only USER role is allowed during self-registration
        if (registerRequest.getRole() != null && registerRequest.getRole() != Role.USER) {
            throw new BadRequestException("Only USER role is allowed for registration");
        }

        // Validate user role
        UserRole userRole = roleRepository.findByRole(Role.USER)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        AppUser appUser = new AppUser();
        appUser.setFirstName(registerRequest.getFirstName());
        appUser.setLastName(registerRequest.getLastName());
        appUser.setEmail(registerRequest.getEmail());
        appUser.setPhone(registerRequest.getPhone());
        appUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        appUser.setUserRole(userRole);
        userRepository.save(appUser);

        return "User saved successfully";

    }

}
