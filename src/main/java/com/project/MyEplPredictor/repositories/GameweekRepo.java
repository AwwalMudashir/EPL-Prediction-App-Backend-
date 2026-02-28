package com.project.MyEplPredictor.repositories;

import com.project.MyEplPredictor.models.Gameweek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameweekRepo extends JpaRepository<Gameweek,Long> {
}
