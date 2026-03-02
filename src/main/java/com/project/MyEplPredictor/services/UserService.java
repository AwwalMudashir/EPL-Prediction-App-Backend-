package com.project.MyEplPredictor.services;

import com.project.MyEplPredictor.DTO.LoginDto;
import com.project.MyEplPredictor.DTO.LoginResponse;
import com.project.MyEplPredictor.DTO.UserDto;
import com.project.MyEplPredictor.models.User;
import com.project.MyEplPredictor.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;



    public ResponseEntity<?> register(UserDto userDto) {
        if (userRepo.existsByEmail(userDto.getEmail())){
            return new ResponseEntity<>("Email Already Exists", HttpStatus.BAD_REQUEST);
        }

        User user = new User(userDto);
        user.setEmail(userDto.getEmail());
        user.setUsername(userDto.getUsername());
        user.setPassword(encoder.encode(userDto.getPassword()));

        try {
            return new ResponseEntity<>(userRepo.save(user), HttpStatus.OK);
        } catch (DataIntegrityViolationException dive) {
            // possibly another request inserted same email concurrently
            return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> login(LoginDto request){

        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }


        if (!userRepo.existsByEmail(request.getEmail())){
            return new ResponseEntity<>("User doesn't exist",HttpStatus.BAD_REQUEST);
        }

        User user = userRepo.findByEmail(request.getEmail());



        String jwt = jwtService.generateToken(user);

        LoginResponse response = new LoginResponse();
        response.setToken(jwt);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> allUsers() {
        try{
            return new ResponseEntity<>(userRepo.findAll(),HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // helper for controller use
    public User findByEmail(String email) {
        return userRepo.findByEmail(email);
    }
}
