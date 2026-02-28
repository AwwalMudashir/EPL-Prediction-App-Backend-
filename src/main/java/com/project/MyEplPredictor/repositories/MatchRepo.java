package com.project.MyEplPredictor.repositories;

import com.project.MyEplPredictor.models.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepo extends JpaRepository<Match,Long> {
}
