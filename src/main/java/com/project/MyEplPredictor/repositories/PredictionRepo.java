package com.project.MyEplPredictor.repositories;

import com.project.MyEplPredictor.models.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepo extends JpaRepository<Prediction,Long> {
	// derived query sometimes fails to parse the nested property path
	// (pMatch is an awkward name for the relationship), resulting in
	// "Binding property is null" at runtime.  Use an explicit JPQL
	// statement so Spring Data doesn’t have to interpret the method
	// name.
	@Query("select case when count(p)>0 then true else false end " +
		"from Prediction p where p.pMatch.id = :matchId and p.user.id = :userId")
	boolean existsByPMatch_IdAndUser_Id(@Param("matchId") Long matchId,
						@Param("userId") Long userId);
	// Spring Data derived query had difficulty resolving the nested property path
	// when the match field was named "pMatch".  An explicit JPQL query avoids
	// the runtime "Binding property is null" error.
	@Query("select p from Prediction p where p.user.id = :userId " +
			"and p.pMatch.gameweek.id = :gameweekId")
	List<Prediction> findForUserAndGameweek(@Param("userId") Long userId,
							   @Param("gameweekId") Long gameweekId);
	List<Prediction> findByPMatch_Id(Long matchId);
	List<Prediction> findByPMatch_IdAndPointsAwardedIsNull(Long matchId);
	List<Prediction> findByUser_Id(Long userId);
	List<Prediction> findByUser_IdIn(List<Long> userIds);

	// convenience method used by global leaderboard calculation
	List<Prediction> findByPointsAwardedIsNotNull();
}
