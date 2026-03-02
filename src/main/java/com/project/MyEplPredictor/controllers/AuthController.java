package com.project.MyEplPredictor.controllers;

import com.project.MyEplPredictor.DTO.LoginDto;
import com.project.MyEplPredictor.DTO.UserDto;
import com.project.MyEplPredictor.DTO.UserSummaryDto;
import com.project.MyEplPredictor.models.User;
import com.project.MyEplPredictor.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto user){
        return userService.register(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto user){
        return userService.login(user);
    }

    @GetMapping("all")
    public ResponseEntity<?> allUsers(){
        return userService.allUsers();
    }

    // return summary of currently authenticated user
    @GetMapping("/me")
    public ResponseEntity<?> currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        UserSummaryDto dto = new UserSummaryDto(user.getId(), user.getUsername(), user.getEmail());
        return ResponseEntity.ok(dto);
    }
}
