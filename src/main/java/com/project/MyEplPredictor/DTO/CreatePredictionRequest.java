package com.project.MyEplPredictor.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreatePredictionRequest {
    private Long userId;
    private Long matchId;
    private Integer predictedHomeScore;
    private Integer predictedAwayScore;
}
