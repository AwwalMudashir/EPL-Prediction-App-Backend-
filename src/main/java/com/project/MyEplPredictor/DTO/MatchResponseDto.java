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
public class MatchResponseDto {
    private Long id;
    private String fixtureId;
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime kickoffTime;
    private Integer homeScore;
    private Integer awayScore;
    private MatchStatus status;
}
