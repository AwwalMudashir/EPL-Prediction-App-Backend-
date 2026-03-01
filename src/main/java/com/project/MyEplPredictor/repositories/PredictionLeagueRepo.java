package com.project.MyEplPredictor.repositories;

import com.project.MyEplPredictor.models.PredictionLeague;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PredictionLeagueRepo extends JpaRepository<PredictionLeague,Long> {
	Optional<PredictionLeague> findByInviteCode(String inviteCode);
}
