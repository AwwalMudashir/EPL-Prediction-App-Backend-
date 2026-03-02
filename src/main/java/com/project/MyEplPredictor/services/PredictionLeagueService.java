package com.project.MyEplPredictor.services;

import com.project.MyEplPredictor.DTO.*;
import com.project.MyEplPredictor.models.LeagueMember;
import com.project.MyEplPredictor.models.PredictionLeague;
import com.project.MyEplPredictor.models.User;
import com.project.MyEplPredictor.repositories.LeagueMemberRepo;
import com.project.MyEplPredictor.repositories.PredictionLeagueRepo;
import com.project.MyEplPredictor.repositories.UserRepo;
import com.project.MyEplPredictor.repositories.PredictionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PredictionLeagueService {

    @Autowired
    private PredictionLeagueRepo plrepo;

    @Autowired
    private LeagueMemberRepo lmrepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PredictionRepo predictionRepo;

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomCode(int length) {
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            // Get a random index from 0 to the length of the character pool
            int index = random.nextInt(ALPHA_NUMERIC_STRING.length());

            // Append the character at that index
            builder.append(ALPHA_NUMERIC_STRING.charAt(index));
        }

        return builder.toString();
    }

    public ResponseEntity<?> createLeague(LeagueDto league) {
        try {
            if (league == null || league.getUserId() == null) {
                return new ResponseEntity<>("User id must be provided", HttpStatus.BAD_REQUEST);
            }
            Long id = league.getUserId();
            String name = league.getName();
            if (name == null || name.isBlank()) {
                return new ResponseEntity<>("League name must be provided", HttpStatus.BAD_REQUEST);
            }

            User userCreated = userRepo.findById(id)
                    .orElse(null);

            if (userCreated == null) {
                return new ResponseEntity<>("User doesn't exist", HttpStatus.BAD_REQUEST);
            }

            String uniqueCode = generateRandomCode(8);

            PredictionLeague newLeague = new PredictionLeague();
            newLeague.setCreatedBy(userCreated);
            newLeague.setName(name);
            newLeague.setInviteCode(uniqueCode);

            LeagueMember leagueMember = new LeagueMember();
            leagueMember.setUser(userCreated);
            leagueMember.setLeague(newLeague);

            newLeague.getMembers().add(leagueMember);
            PredictionLeague savedLeague = plrepo.save(newLeague);

            return new ResponseEntity<>(mapToResponseDto(savedLeague), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean doesLeagueMemberExist(Long userId, Long leagueId){
        return lmrepo.existsByUser_IdAndLeague_Id(userId, leagueId);
    }

    public ResponseEntity<?> joinLeague(Long userId, String inviteCode) {
        try {
            User user = userRepo.findById(userId)
                    .orElse(null);

            if (user == null) {
                return new ResponseEntity<>("User doesn't exist", HttpStatus.BAD_REQUEST);
            }

            PredictionLeague league = plrepo.findByInviteCode(inviteCode)
                    .orElse(null);

            if (league == null) {
                return new ResponseEntity<>("This League doesn't exist", HttpStatus.BAD_REQUEST);
            }

            if (doesLeagueMemberExist(userId, league.getId())) {
                return new ResponseEntity<>("You are Already in This League", HttpStatus.BAD_REQUEST);
            }

            LeagueMember leagueMember = new LeagueMember();
            leagueMember.setUser(user);
            leagueMember.setLeague(league);

            lmrepo.save(leagueMember);
            league.getMembers().add(leagueMember);
            PredictionLeague updatedLeague = plrepo.save(league);

            return new ResponseEntity<>(mapToResponseDto(updatedLeague), HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> getUserLeagues(Long userId) {
        try {
            if (!userRepo.existsById(userId)) {
                return new ResponseEntity<>("User doesn't exist", HttpStatus.BAD_REQUEST);
            }

            List<LeagueMember> memberships = lmrepo.findByUser_Id(userId);

            List<PredictionLeagueResponseDto> leagues = memberships.stream()
                    .map(LeagueMember::getLeague)
                    .filter(Objects::nonNull)
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(leagues);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> getLeaguesCreatedByUser(Long userId) {
        try {
            if (!userRepo.existsById(userId)) {
                return new ResponseEntity<>("User doesn't exist", HttpStatus.BAD_REQUEST);
            }

            List<PredictionLeague> leagues = plrepo.findByCreatedBy_Id(userId);
            List<PredictionLeagueResponseDto> dtos = leagues.stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> getLeague(Long leagueId) {
        try {
            PredictionLeague league = plrepo.findById(leagueId).orElse(null);
            if (league == null) {
                return new ResponseEntity<>("League not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(mapToResponseDto(league));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PredictionLeagueResponseDto mapToResponseDto(PredictionLeague league) {
        UserSummaryDto creator = toUserSummary(league.getCreatedBy());

        List<LeagueMemberResponseDto> members = league.getMembers() == null
                ? List.of()
                : league.getMembers()
                .stream()
                .map(this::toMemberDto)
                .collect(Collectors.toList());

        PredictionLeagueResponseDto dto = new PredictionLeagueResponseDto();
        dto.setId(league.getId());
        dto.setName(league.getName());
        dto.setInviteCode(league.getInviteCode());
        dto.setCreatedAt(league.getCreatedAt());
        dto.setCreatedBy(creator);
        dto.setMembers(members);
        return dto;
    }

    public ResponseEntity<?> getLeagueStandings(Long leagueId) {
        try {
            PredictionLeague league = plrepo.findById(leagueId)
                    .orElse(null);

            if (league == null) {
                return new ResponseEntity<>("League not found", HttpStatus.NOT_FOUND);
            }

            List<LeagueMember> members = league.getMembers();
            if (members == null || members.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            List<Long> userIds = members.stream()
                    .map(member -> member.getUser().getId())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            List<LeagueStandingDto> standings = buildStandings(userIds, members);
            return ResponseEntity.ok(standings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<LeagueStandingDto> buildStandings(List<Long> userIds, List<LeagueMember> members) {
        Map<Long, Integer> totals = new HashMap<>();
        if (!userIds.isEmpty()) {
            predictionRepo.findByUser_IdIn(userIds).forEach(prediction -> {
                if (prediction.getPointsAwarded() == null) {
                    return;
                }
                Long uid = prediction.getUser().getId();
                totals.merge(uid, prediction.getPointsAwarded(), Integer::sum);
            });
        }

        List<LeagueStandingDto> standings = members.stream()
                .map(member -> {
                    UserSummaryDto userSummary = toUserSummary(member.getUser());
                    int points = totals.getOrDefault(member.getUser().getId(), 0);
                    return new LeagueStandingDto(userSummary, points, 0);
                })
                .collect(Collectors.toList());

        standings.sort(Comparator.comparingInt(LeagueStandingDto::getTotalPoints).reversed());

        int rank = 0;
        int lastPoints = Integer.MIN_VALUE;
        int tiedCount = 0;
        for (LeagueStandingDto standing : standings) {
            if (standing.getTotalPoints() != lastPoints) {
                rank += 1 + tiedCount;
                tiedCount = 0;
                lastPoints = standing.getTotalPoints();
            } else {
                tiedCount++;
            }
            standing.setRank(rank);
        }

        return standings;
    }

    private LeagueMemberResponseDto toMemberDto(LeagueMember member) {
        LeagueMemberResponseDto dto = new LeagueMemberResponseDto();
        dto.setId(member.getId());
        dto.setJoinedAt(member.getJoinedAt());
        dto.setUser(toUserSummary(member.getUser()));
        return dto;
    }

    private UserSummaryDto toUserSummary(User user) {
        if (user == null) {
            return null;
        }
        UserSummaryDto dto = new UserSummaryDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public ResponseEntity<?> getAllLeagues() {
        try{
            return new ResponseEntity<>(plrepo.findAll(),HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
