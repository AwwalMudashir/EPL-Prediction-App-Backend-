package com.project.MyEplPredictor.controllers;

import com.project.MyEplPredictor.DTO.CreatePredictionRequest;
import com.project.MyEplPredictor.DTO.PredictionResponseDto;
import com.project.MyEplPredictor.DTO.UpdatePredictionRequest;
import com.project.MyEplPredictor.services.PredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/predictions")
public class PredictionController {

	private final PredictionService predictionService;

	public PredictionController(PredictionService predictionService) {
		this.predictionService = predictionService;
	}

	@PostMapping
	public ResponseEntity<PredictionResponseDto> createPrediction(@RequestBody CreatePredictionRequest request) {
		return ResponseEntity.ok(predictionService.createPrediction(request));
	}

	@PutMapping("/{predictionId}")
	public ResponseEntity<PredictionResponseDto> updatePrediction(@PathVariable Long predictionId,
																  @RequestBody UpdatePredictionRequest request) {
		return ResponseEntity.ok(predictionService.updatePrediction(predictionId, request));
	}

	@GetMapping("/user/{userId}/gameweek/{gameweekId}")
	public ResponseEntity<List<PredictionResponseDto>> getUserPredictions(@PathVariable Long userId,
																		  @PathVariable Long gameweekId) {
		return ResponseEntity.ok(predictionService.getUserPredictionsForGameweek(userId, gameweekId));
	}

	@PostMapping("/score/match/{matchId}")
	public ResponseEntity<Void> scoreMatch(@PathVariable Long matchId) {
		predictionService.scorePredictionsForMatch(matchId);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/score/gameweek/{gameweekId}")
	public ResponseEntity<Void> scoreGameweek(@PathVariable Long gameweekId) {
		predictionService.scorePredictionsForGameweek(gameweekId);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/recalculate/match/{matchId}")
	public ResponseEntity<Void> recalculateMatch(@PathVariable Long matchId) {
		predictionService.recalculatePredictionsForMatch(matchId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/user/{userId}/points")
	public ResponseEntity<Map<Long, Integer>> getUserPoints(@PathVariable Long userId) {
		return ResponseEntity.ok(predictionService.getUserPointsByGameweek(userId));
	}
}
