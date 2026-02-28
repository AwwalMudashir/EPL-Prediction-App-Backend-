package com.project.MyEplPredictor.controllers;

import com.project.MyEplPredictor.DTO.LoginDto;
import com.project.MyEplPredictor.DTO.UserDto;
import com.project.MyEplPredictor.models.User;
import com.project.MyEplPredictor.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
