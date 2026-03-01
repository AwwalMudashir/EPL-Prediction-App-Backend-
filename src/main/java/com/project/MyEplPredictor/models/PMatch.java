package com.project.MyEplPredictor.models;

import com.project.MyEplPredictor.DTO.MatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 'MATCH' is a reserved word in many databases (including MySQL),
// so we explicitly map the entity to a different table name.  Using a
// pluralized form also better reflects the collection semantics.
@Entity
@Table(name = "matches")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class PMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fixtureId;
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime kickoffTime;
    private Integer homeScore;
    private Integer awayScore;

    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.UPCOMING;

    @ManyToOne
    @JoinColumn(name = "gameweek_id")
    private Gameweek gameweek;
}
