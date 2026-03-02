package com.project.MyEplPredictor.config;


import com.project.MyEplPredictor.services.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(customizer -> customizer.disable()); // disable csrf
        http.cors(Customizer.withDefaults()); // enable CORS using the bean defined below
        http.authorizeHttpRequests(request -> request
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll() // allow preflight
                .requestMatchers("/user/register","/user/login").permitAll()
                .anyRequest().authenticated()); // means that the user has to sign in to access any endpoint
        // http.formLogin(Customizer.withDefaults()); // enables the form for the sign in, for the browser (frontend basically)
        http.httpBasic(Customizer.withDefaults()); // enables the form for the sign in, with post man

//        We use the below to disable csrf and by making http stateless, so everytime the user
//        reloads or enters the site they get a new session id, on postman it works well
//        but to work on browser you have to disable line, 38, the formLogin method
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        // above, i'm adding the jwtFilter I created before the UPAF so the JWT token works
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(12);
    }

    // CORS configuration allowing the frontend origin to communicate with the API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow the frontend origins defined in properties (includes local dev and
        // production URL).  The value is a comma‑separated list of origins.
        String origins = System.getProperty("app.allowed.origins");
        if (origins == null) {
            origins = "http://localhost:3000,http://localhost:5174,https://epl-predictor.netlify.app/,https://epl-predictor-frontend-prod.vercel.app/";
        }
        configuration.setAllowedOrigins(Arrays.asList(origins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService(){
        return new MyUserDetailsService();
    }

    //    Customizing my own Authentication Provider
    @Bean
    public AuthenticationProvider authenticationProvider(){
//        DaoAuthenticationProvider is for databases
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService());
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // The Authentication Manager handles auth first before moving to authentication
        // provider.

        // Because AuthenticationManager is an interface, we use an object of Authentication
        // configuration which has a method that return AuthenticationManager

        return config.getAuthenticationManager();
    }
}
