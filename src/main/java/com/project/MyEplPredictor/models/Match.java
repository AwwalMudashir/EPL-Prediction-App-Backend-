package com.project.MyEplPredictor.models;

import com.project.MyEplPredictor.DTO.MatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fixtureId;
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime kickoffTime;
    private int homeScore;
    private int awayScore;
    private MatchStatus status;

    @ManyToOne
    @JoinColumn(name = "gameweek_id")
    private Gameweek gameweek;
}
