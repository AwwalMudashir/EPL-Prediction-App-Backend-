package com.project.MyEplPredictor.repositories;

import com.project.MyEplPredictor.models.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepo extends JpaRepository<Prediction,Long> {
	boolean existsByMatch_IdAndUser_Id(Long matchId, Long userId);
	List<Prediction> findByUser_IdAndMatch_Gameweek_Id(Long userId, Long gameweekId);
	List<Prediction> findByMatch_Id(Long matchId);
	List<Prediction> findByMatch_IdAndPointsAwardedIsNull(Long matchId);
	List<Prediction> findByUser_Id(Long userId);
	List<Prediction> findByUser_IdIn(List<Long> userIds);
}
