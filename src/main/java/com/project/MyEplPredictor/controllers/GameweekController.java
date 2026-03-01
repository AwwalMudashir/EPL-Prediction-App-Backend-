package com.project.MyEplPredictor.controllers;

import com.project.MyEplPredictor.DTO.GameweekResponseDto;
import com.project.MyEplPredictor.DTO.MatchResponseDto;
import com.project.MyEplPredictor.services.GameweekService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gameweeks")
public class GameweekController {

	private final GameweekService gameweekService;

	public GameweekController(GameweekService gameweekService) {
		this.gameweekService = gameweekService;
	}

	@GetMapping("/current")
	public ResponseEntity<GameweekResponseDto> getCurrentGameweek() {
		return ResponseEntity.ok(gameweekService.getCurrentGameweek());
	}

	@GetMapping("/current/matches")
	public ResponseEntity<List<MatchResponseDto>> getCurrentGameweekMatches() {
		return ResponseEntity.ok(gameweekService.getCurrentGameweekMatches());
	}

	@PostMapping("/{gameweekId}/activate")
	public ResponseEntity<GameweekResponseDto> activateGameweek(@PathVariable Long gameweekId) {
		return ResponseEntity.ok(gameweekService.activateGameweek(gameweekId));
	}

	@PostMapping("/{gameweekId}/lock")
	public ResponseEntity<GameweekResponseDto> lockGameweek(@PathVariable Long gameweekId) {
		return ResponseEntity.ok(gameweekService.lockGameweekIfStarted(gameweekId));
	}

	@PostMapping("/{gameweekId}/complete")
	public ResponseEntity<GameweekResponseDto> completeGameweek(@PathVariable Long gameweekId) {
		return ResponseEntity.ok(gameweekService.completeGameweekIfFinished(gameweekId));
	}

	@PostMapping("/assign-matches")
	public ResponseEntity<List<MatchResponseDto>> assignMatchesToGameweeks() {
		return ResponseEntity.ok(gameweekService.assignMatchesToGameweeks());
	}

	@PostMapping("/sync-fixtures")
	public ResponseEntity<List<MatchResponseDto>> syncFixturesFromApi() {
		return ResponseEntity.ok(gameweekService.syncFixturesFromApi());
	}

	// explicit creation endpoint described in architecture notes
	@PostMapping("/create")
	public ResponseEntity<com.project.MyEplPredictor.DTO.CreateGameweekResponseDto> createGameweek() {
		return ResponseEntity.ok(gameweekService.createGameweek());
	}
}
