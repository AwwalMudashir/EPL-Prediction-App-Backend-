package com.project.MyEplPredictor.repositories;

import com.project.MyEplPredictor.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
    User findByUsername(String username);
    User findByEmail(String email);

    Boolean existsByEmail(String email);
}
