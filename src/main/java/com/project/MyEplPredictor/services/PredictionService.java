package com.project.MyEplPredictor.services;

import com.project.MyEplPredictor.DTO.CreatePredictionRequest;
import com.project.MyEplPredictor.DTO.MatchStatus;
import com.project.MyEplPredictor.DTO.PredictionResponseDto;
import com.project.MyEplPredictor.DTO.UpdatePredictionRequest;
import com.project.MyEplPredictor.models.Gameweek;
import com.project.MyEplPredictor.models.GameweekStatus;
import com.project.MyEplPredictor.models.PMatch;
import com.project.MyEplPredictor.models.Prediction;
import com.project.MyEplPredictor.models.User;
import com.project.MyEplPredictor.repositories.MatchRepo;
import com.project.MyEplPredictor.repositories.PredictionRepo;
import com.project.MyEplPredictor.repositories.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PredictionService {

	private final PredictionRepo predictionRepo;
	private final MatchRepo matchRepo;
	private final UserRepo userRepo;

	public PredictionService(PredictionRepo predictionRepo,
							 MatchRepo matchRepo,
							 UserRepo userRepo) {
		this.predictionRepo = predictionRepo;
		this.matchRepo = matchRepo;
		this.userRepo = userRepo;
	}

	public PredictionResponseDto createPrediction(CreatePredictionRequest request) {
		PMatch PMatch = matchRepo.findById(request.getMatchId())
				.orElseThrow(() -> new IllegalArgumentException("Match not found"));
		ensurePredictionWindowOpen(PMatch);

		User user = userRepo.findById(request.getUserId())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		boolean alreadyPredicted = predictionRepo.existsByMatch_IdAndUser_Id(PMatch.getId(), user.getId());
		if (alreadyPredicted) {
			throw new IllegalStateException("Prediction already submitted for this match");
		}

		Prediction prediction = new Prediction();
		prediction.setPMatch(PMatch);
		prediction.setUser(user);
		prediction.setPredictedHomeScore(request.getPredictedHomeScore());
		prediction.setPredictedAwayScore(request.getPredictedAwayScore());
		prediction.setPointsAwarded(null);

		Prediction saved = predictionRepo.save(prediction);
		return toDto(saved);
	}

	@Transactional(readOnly = true)
	public List<PredictionResponseDto> getUserPredictionsForGameweek(Long userId, Long gameweekId) {
		List<Prediction> predictions = predictionRepo.findByUser_IdAndMatch_Gameweek_Id(userId, gameweekId);
		return predictions.stream().map(this::toDto).collect(Collectors.toList());
	}

	public PredictionResponseDto updatePrediction(Long predictionId, UpdatePredictionRequest request) {
		Prediction prediction = predictionRepo.findById(predictionId)
				.orElseThrow(() -> new IllegalArgumentException("Prediction not found"));
		ensurePredictionWindowOpen(prediction.getPMatch());

		prediction.setPredictedHomeScore(request.getPredictedHomeScore());
		prediction.setPredictedAwayScore(request.getPredictedAwayScore());
		prediction.setPointsAwarded(null);

		return toDto(predictionRepo.save(prediction));
	}

	public void scorePredictionsForMatch(Long matchId) {
		PMatch PMatch = matchRepo.findById(matchId)
				.orElseThrow(() -> new IllegalArgumentException("Match not found"));

		if (PMatch.getStatus() != MatchStatus.FINISHED) {
			throw new IllegalStateException("Match is not finished yet");
		}

		if (PMatch.getHomeScore() == null || PMatch.getAwayScore() == null) {
			throw new IllegalStateException("Match scores missing");
		}

		List<Prediction> pending = predictionRepo.findByMatch_IdAndPointsAwardedIsNull(matchId);
		if (pending.isEmpty()) {
			return;
		}

		for (Prediction prediction : pending) {
			int points = calculatePoints(prediction, PMatch);
			prediction.setPointsAwarded(points);
		}
		predictionRepo.saveAll(pending);
	}

	public void scorePredictionsForGameweek(Long gameweekId) {
		List<PMatch> PMatches = matchRepo.findByGameweekId(gameweekId);
		for (PMatch PMatch : PMatches) {
			if (PMatch.getStatus() == MatchStatus.FINISHED) {
				scorePredictionsForMatch(PMatch.getId());
			}
		}
	}

	public void recalculatePredictionsForMatch(Long matchId) {
		List<Prediction> predictions = predictionRepo.findByMatch_Id(matchId);
		for (Prediction prediction : predictions) {
			prediction.setPointsAwarded(null);
		}
		predictionRepo.saveAll(predictions);
		scorePredictionsForMatch(matchId);
	}

	@Transactional(readOnly = true)
	public Map<Long, Integer> getUserPointsByGameweek(Long userId) {
		List<Prediction> predictions = predictionRepo.findByUser_Id(userId);
		Map<Long, Integer> totals = new HashMap<>();

		for (Prediction prediction : predictions) {
			Integer points = prediction.getPointsAwarded();
			if (points == null) {
				continue;
			}
			Gameweek gameweek = prediction.getPMatch().getGameweek();
			if (gameweek == null) {
				continue;
			}
			totals.merge(gameweek.getId(), points, Integer::sum);
		}
		return totals;
	}

	private void ensurePredictionWindowOpen(PMatch PMatch) {
		if (PMatch.getStatus() != MatchStatus.UPCOMING) {
			throw new IllegalStateException("Predictions closed for this match");
		}

		LocalDateTime kickoff = PMatch.getKickoffTime();
		if (kickoff != null && !LocalDateTime.now().isBefore(kickoff)) {
			throw new IllegalStateException("Match already started");
		}

		Gameweek gameweek = PMatch.getGameweek();
		if (gameweek == null) {
			throw new IllegalStateException("Match is not assigned to a gameweek");
		}

		if (gameweek.getStatus() == GameweekStatus.LOCKED || gameweek.getStatus() == GameweekStatus.COMPLETED) {
			throw new IllegalStateException("Gameweek is locked");
		}
	}

	private int calculatePoints(Prediction prediction, PMatch PMatch) {
		int predictedHome = prediction.getPredictedHomeScore();
		int predictedAway = prediction.getPredictedAwayScore();
		int actualHome = PMatch.getHomeScore();
		int actualAway = PMatch.getAwayScore();

		if (predictedHome == actualHome && predictedAway == actualAway) {
			return 3;
		}

		int predictedResult = Integer.compare(predictedHome, predictedAway);
		int actualResult = Integer.compare(actualHome, actualAway);

		return predictedResult == actualResult ? 1 : 0;
	}

	private PredictionResponseDto toDto(Prediction prediction) {
		PMatch PMatch = prediction.getPMatch();
		PredictionResponseDto dto = new PredictionResponseDto();
		dto.setPredictionId(prediction.getId());
		dto.setMatchId(PMatch.getId());
		dto.setHomeTeam(PMatch.getHomeTeam());
		dto.setAwayTeam(PMatch.getAwayTeam());
		dto.setKickoffTime(PMatch.getKickoffTime());
		dto.setPredictedHomeScore(prediction.getPredictedHomeScore());
		dto.setPredictedAwayScore(prediction.getPredictedAwayScore());
		dto.setActualHomeScore(PMatch.getHomeScore());
		dto.setActualAwayScore(PMatch.getAwayScore());
		dto.setPointsAwarded(prediction.getPointsAwarded());
		dto.setStatus(PMatch.getStatus());
		return dto;
	}
}
