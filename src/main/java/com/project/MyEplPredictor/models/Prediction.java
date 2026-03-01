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
    private PMatch PMatch;
    private Integer pointsAwarded;
    private int predictedHomeScore;
    private int predictedAwayScore;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
