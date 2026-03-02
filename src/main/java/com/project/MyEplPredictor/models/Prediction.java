package com.project.MyEplPredictor.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Prediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private PMatch pMatch;

    // pointsAwarded is intentionally nullable – it remains null until the
    // match has finished and scoring is applied.  the underlying database
    // column must therefore allow NULL values or inserts/updates will fail.
    @Column(name = "points_awarded", nullable = true)
    private Integer pointsAwarded;

    private int predictedHomeScore;
    private int predictedAwayScore;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
