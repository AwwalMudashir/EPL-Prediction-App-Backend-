package com.project.MyEplPredictor.DTO;

import java.util.List;

public class CreateGameweekResponseDto {
    private GameweekResponseDto gameweek;
    private List<MatchResponseDto> matches;

    public GameweekResponseDto getGameweek() {
        return gameweek;
    }

    public void setGameweek(GameweekResponseDto gameweek) {
        this.gameweek = gameweek;
    }

    public List<MatchResponseDto> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchResponseDto> matches) {
        this.matches = matches;
    }
}