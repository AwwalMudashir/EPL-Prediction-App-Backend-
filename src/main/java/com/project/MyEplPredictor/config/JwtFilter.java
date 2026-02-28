package com.project.MyEplPredictor.config;

import com.project.MyEplPredictor.services.JwtService;
import com.project.MyEplPredictor.services.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//    My User Defined filter is extending OncePerRequestFilter, cos i only want the token to
//    be used once (as the name suggests)

// make it a bean (component)

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    @Qualifier("myUserDetailsService") // Disambiguate if multiple UserDetailsService beans exist.
    private MyUserDetailsService myUserDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request // incoming http request
            , HttpServletResponse response, // http response
                                    FilterChain filterChain // remaining filter chain
    ) throws ServletException, IOException {
//        The clients side looks like
//        Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJib3kiLCJpYXQiOjE3NTU2OTE1ODMsImV4cCI6MTc1NTY5MTYxOX0.-nAkHqIuBN1LrGZfQ9ir7681BKPtzA6QaVxgy5Xza2A

//        So all we have to do is get the token and validate it.

        String authHeader = request.getHeader("Authorization");
        String token = null; // Will hold the raw JWT string if present.
        String username = null; // Will hold the username extracted from the token.

        if (authHeader != null && authHeader.startsWith("Bearer ")){
            token = authHeader.split(" ")[1];
            // Ask JwtService to parse and pull the 'sub' (subject) claim, i.e., the username.
            username = jwtService.extractUsername(token);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            // Load user details (password hash, enabled flags, authorities) from your user store.
            UserDetails userDetails = myUserDetailService.loadUserByUsername(username);

            // Validate that the token:
            // - belongs to this user (subject matches), and
            // - is not expired (and possibly other checks you define).
            if (jwtService.validateToken(token,userDetails)){
                // Build an Authentication object representing this user and their authorities.
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,           // principal
                                null,                  // no credentials here (we used a token)
                                userDetails.getAuthorities() // roles/authorities for access control
                        );

                // Attach request-specific details (IP, session id, etc.) used by some components.
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Put the Authentication into the thread-local SecurityContext so downstream
                // filters/controllers know the user is authenticated.
                SecurityContextHolder.getContext().setAuthentication(authToken);

            }
        }
        // Always continue the filter chain—this filter doesn't write the response here.
        filterChain.doFilter(request,response);

    }
}

/*
OncePerRequestFilter: guarantees your token logic runs once per request.

SecurityContextHolder: Spring Security’s thread-local store for the current user’s Authentication.

UsernamePasswordAuthenticationToken: a concrete Authentication implementation you use to signal an authenticated principal with authorities (even though you didn’t re-check the password, JWT already did the job).
 */
