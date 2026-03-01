package com.project.MyEplPredictor.repositories;

import com.project.MyEplPredictor.models.Gameweek;
import com.project.MyEplPredictor.models.GameweekStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameweekRepo extends JpaRepository<Gameweek,Long> {
	Optional<Gameweek> findFirstByStatusOrderByStartDateAsc(GameweekStatus status);
	Optional<Gameweek> findFirstByStatusAndStartDateAfterOrderByStartDateAsc(GameweekStatus status, LocalDateTime startAfter);
	List<Gameweek> findByStatus(GameweekStatus status);

    @org.springframework.data.jpa.repository.Query("select max(g.weekNumber) from Gameweek g")
    Integer findMaxWeekNumber();
}
