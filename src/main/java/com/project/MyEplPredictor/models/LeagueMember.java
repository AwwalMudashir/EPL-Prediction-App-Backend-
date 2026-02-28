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
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "league_id"})
        }
)
public class LeagueMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Which user joined?
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /*
     * Which league was joined?
     */
    @ManyToOne
    @JoinColumn(name = "league_id", nullable = false)
    private PredictionLeague league;

    @CreationTimestamp
    private LocalDateTime joinedAt;

}
