package com.shopsphere.config.security.jwt;

import com.shopsphere.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/*
  A JWT filter to validate tokens on incoming requests.

  JwtAuthenticationFilter is a custom implementation of OncePerRequestFilter
  that intercepts incoming HTTP requests to validate and process JWT tokens.
  It ensures secure request handling by setting up the necessary authentication
  in the Spring Security context.

  Responsibilities:
  - Extracts the JWT token from the Authorization header of incoming requests.
  - Validates the extracted JWT to ensure integrity and authenticity.
  - Loads the corresponding UserDetails based on the username extracted from the token.
  - Sets up an authenticated SecurityContext if the token is valid.
  - Proceeds to the next filter in the chain after processing the token.

  Key Dependencies:
  - JwtUtil: A utility class responsible for token generation, extraction, and validation.
  - UserDetailsService: Loads user-specific data during the authentication process.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Same contract as for {@code doFilter}, but guaranteed to be
     * just invoked once per request within a single request thread.
     * See {@link #shouldNotFilterAsyncDispatch()} for details.
     * <p>Provides HttpServletRequest and HttpServletResponse arguments instead of the
     * default ServletRequest and ServletResponse ones.
     *
     * @param request
     * @param response
     * @param filterChain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");


        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); //If not pass to next filter
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username = jwtService.extractUsername(jwt);

        // If username is found and user is not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            //Validate Token
            if (jwtService.isTokenValid(jwt, userDetails)) {
                //create an authentication object if the token is valid
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource()
                        .buildDetails(request));

                // Set the Authentication object in SecurityContextHolder
                // This marks the user as authenticated for the current request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response); //pass to next filter
        }
    }
}
