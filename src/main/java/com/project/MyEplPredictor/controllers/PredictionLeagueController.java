package com.project.MyEplPredictor.controllers;

import com.project.MyEplPredictor.DTO.LeagueDto;
import com.project.MyEplPredictor.services.PredictionLeagueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/league")
public class PredictionLeagueController {

    @Autowired
    private PredictionLeagueService plservice;

    @PostMapping("/create")
    public ResponseEntity<?> createLeague(@RequestBody LeagueDto league){
        return plservice.createLeague(league);
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinLeague(@RequestParam Long userId, @RequestParam String inviteCode){
        return plservice.joinLeague(userId,inviteCode);
    }

    @GetMapping("/user/{userId}/leagues")
    public ResponseEntity<?> getUsersLeagues(@PathVariable Long userId) {
        return plservice.getUserLeagues(userId);
    }

    @GetMapping("/allLeagues")
    public ResponseEntity<?> allLeagues(){
        return plservice.getAllLeagues();
    }

    @GetMapping("/{leagueId}/standings")
    public ResponseEntity<?> getLeagueStandings(@PathVariable Long leagueId) {
        return plservice.getLeagueStandings(leagueId);
    }
}
