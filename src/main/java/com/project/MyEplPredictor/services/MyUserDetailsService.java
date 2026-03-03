package com.project.MyEplPredictor.services;

import com.project.MyEplPredictor.models.User;
import com.project.MyEplPredictor.models.UserPrincipal;
import com.project.MyEplPredictor.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    @Cacheable("usersByEmail")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(username);

        if(user == null){
            System.out.println("User not found !");
            throw new UsernameNotFoundException("User not Found");
        }

        return new UserPrincipal(user);
    }
}
