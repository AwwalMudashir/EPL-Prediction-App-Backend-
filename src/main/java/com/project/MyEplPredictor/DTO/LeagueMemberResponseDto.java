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
public class LeagueMemberResponseDto {
    private Long id;
    private UserSummaryDto user;
    private LocalDateTime joinedAt;
}
