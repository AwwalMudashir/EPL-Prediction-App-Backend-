package com.project.MyEplPredictor.controllers;

import com.project.MyEplPredictor.DTO.CreatePredictionRequest;
import com.project.MyEplPredictor.DTO.PredictionResponseDto;
import com.project.MyEplPredictor.DTO.UpdatePredictionRequest;
import com.project.MyEplPredictor.DTO.UserPointsDto;
import com.project.MyEplPredictor.services.PredictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/predictions")
public class PredictionController {

    private static final Logger log = LoggerFactory.getLogger(PredictionController.class);
    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping
    public ResponseEntity<PredictionResponseDto> createPrediction(@RequestBody CreatePredictionRequest request) {
        log.info("createPrediction request = {}", request);
        if (request == null || request.getUserId() == null || request.getMatchId() == null) {
            throw new IllegalArgumentException("userId and matchId are required");
        }
        return ResponseEntity.ok(predictionService.createPrediction(request));
    }

    @PutMapping("/{predictionId}")
    public ResponseEntity<PredictionResponseDto> updatePrediction(@PathVariable Long predictionId,
                                                                  @RequestBody UpdatePredictionRequest request) {
        log.info("updatePrediction id={} body={}", predictionId, request);
        if (request == null) {
            throw new IllegalArgumentException("request body required");
        }
        return ResponseEntity.ok(predictionService.updatePrediction(predictionId, request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PredictionResponseDto>> getUserPredictions(@PathVariable Long userId) {
        return ResponseEntity.ok(predictionService.getUserPredictions(userId));
    }

    @GetMapping("/user/{userId}/gameweek/{gameweekId}")
    public ResponseEntity<List<PredictionResponseDto>> getUserPredictionsForGameweek(@PathVariable Long userId,
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

    @GetMapping("/leaderboard/global")
    public ResponseEntity<List<UserPointsDto>> getGlobalLeaderboard() {
        return ResponseEntity.ok(predictionService.getGlobalPoints());
    }
}
