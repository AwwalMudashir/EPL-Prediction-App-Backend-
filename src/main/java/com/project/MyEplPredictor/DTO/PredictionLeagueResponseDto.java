package com.project.MyEplPredictor.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PredictionLeagueResponseDto {
    private Long id;
    private String name;
    private String inviteCode;
    private LocalDateTime createdAt;
    private UserSummaryDto createdBy;
    private List<LeagueMemberResponseDto> members;
}
