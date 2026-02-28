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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(customizer -> customizer.disable()); // disable csrf
        http.authorizeHttpRequests(request -> request
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
