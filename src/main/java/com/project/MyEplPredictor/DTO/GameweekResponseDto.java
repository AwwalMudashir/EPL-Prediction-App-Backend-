package com.project.MyEplPredictor.DTO;

import com.project.MyEplPredictor.models.GameweekStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GameweekResponseDto {
    private Long id;
    private int weekNumber;
    private int season;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private GameweekStatus status;
}
