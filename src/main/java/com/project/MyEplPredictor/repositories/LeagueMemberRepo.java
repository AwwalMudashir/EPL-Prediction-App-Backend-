package com.project.MyEplPredictor.repositories;

import com.project.MyEplPredictor.models.LeagueMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeagueMemberRepo extends JpaRepository<LeagueMember,Long> {
	boolean existsByUser_IdAndLeague_Id(Long userId, Long leagueId);
	List<LeagueMember> findByUser_Id(Long userId);
}
