package com.control.water.services;

import com.control.water.models.User;
import com.control.water.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user: {}", username);
        
        User user = userRepository.findByEmail(username);
        
        if (user == null) {
            logger.error("User not found with email: {}", username);
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        logger.info("User found: {}", user.getEmail());
        logger.debug("Password hash: {}", user.getPassword());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}