package com.pavan.employeemanagement.security;

import com.pavan.employeemanagement.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Marks this class as a Spring Bean
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Service used to validate and read JWT tokens
    private final JwtService jwt;

    // Repository used to fetch user details from the database
    private final UserRepository users;

    // Constructor Injection
    public JwtAuthenticationFilter(JwtService jwt, UserRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    // This method runs once for every incoming HTTP request
    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain)
            throws ServletException, IOException {

        // Read the Authorization header from the request
        String header = req.getHeader("Authorization");

        // Check if the Authorization header exists and starts with "Bearer "
        if (header != null && header.startsWith("Bearer ")) {

            // Remove "Bearer " and extract the JWT token
            String token = header.substring(7);

            // Get the email (subject) stored inside the JWT token
            String email = jwt.subject(token);

            // Continue only if email exists and user is not already authenticated
            if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                // Find the user in the database using the email
                users.findByEmailIgnoreCase(email)

                        // Check whether the user account is enabled
                        .filter(user -> user.isEnabled())

                        // Execute the following block if the user exists
                        .ifPresent(user -> {

                            // Create authentication object with user's role
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            user.getEmail(),
                                            null,
                                            List.of(
                                                    new SimpleGrantedAuthority(
                                                            "ROLE_" + user.getRole().name()
                                                    )
                                            )
                                    );

                            // Store authentication in Spring Security Context
                            SecurityContextHolder
                                    .getContext()
                                    .setAuthentication(authentication);
                        });
            }
        }

        // Pass the request to the next filter in the filter chain
        chain.doFilter(req, res);
    }
}