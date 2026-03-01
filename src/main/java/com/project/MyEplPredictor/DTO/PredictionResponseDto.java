package com.project.MyEplPredictor.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PredictionResponseDto {
    private Long predictionId;
    private Long matchId;
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime kickoffTime;
    private Integer predictedHomeScore;
    private Integer predictedAwayScore;
    private Integer actualHomeScore;
    private Integer actualAwayScore;
    private Integer pointsAwarded;
    private MatchStatus status;
}
