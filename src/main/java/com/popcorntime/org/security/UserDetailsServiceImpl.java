package com.popcorntime.org.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.popcorntime.org.entity.AppUser;
import com.popcorntime.org.repository.UserRepository;

@Service
/**
 * Service class for loading user-specific data during authentication.
 * Implements Spring Security's UserDetailsService interface.
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Loads user details from the database by email for Spring Security authentication.
     * Maps the internal AppUser entity to a Spring Security UserDetails object.
     *
     * @param username The email address of the user.
     * @return The UserDetails object containing the user's credentials and authorities.
     * @throws UsernameNotFoundException If no user is found with the provided email.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(appUser.getEmail())
                .password(appUser.getPassword())
                .authorities(appUser.getUserRole().getRole().name())
                .build();
    }
}
