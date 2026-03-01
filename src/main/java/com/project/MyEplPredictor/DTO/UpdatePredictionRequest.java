package com.project.MyEplPredictor.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdatePredictionRequest {
    private int predictedHomeScore;
    private int predictedAwayScore;
}
