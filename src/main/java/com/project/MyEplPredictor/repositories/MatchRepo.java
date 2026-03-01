package com.project.MyEplPredictor.repositories;

import com.project.MyEplPredictor.DTO.MatchStatus;
import com.project.MyEplPredictor.models.PMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepo extends JpaRepository<PMatch,Long> {
	List<PMatch> findByGameweekIdOrderByKickoffTimeAsc(Long gameweekId);
	Optional<PMatch> findFirstByGameweekIdOrderByKickoffTimeAsc(Long gameweekId);
	boolean existsByGameweekIdAndStatusNot(Long gameweekId, MatchStatus status);
	List<PMatch> findByGameweekId(Long gameweekId);
	Optional<PMatch> findByFixtureId(String fixtureId);
	List<PMatch> findByStatusIn(List<MatchStatus> statuses);
	List<PMatch> findByKickoffTimeBetween(LocalDateTime start, LocalDateTime end);
}
