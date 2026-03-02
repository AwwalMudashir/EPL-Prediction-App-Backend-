package com.project.MyEplPredictor.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.MyEplPredictor.DTO.CreateGameweekResponseDto;
import com.project.MyEplPredictor.DTO.GameweekResponseDto;
import com.project.MyEplPredictor.DTO.MatchResponseDto;
import com.project.MyEplPredictor.DTO.MatchStatus;
import com.project.MyEplPredictor.models.Gameweek;
import com.project.MyEplPredictor.models.GameweekStatus;
import com.project.MyEplPredictor.models.PMatch;
import com.project.MyEplPredictor.repositories.GameweekRepo;
import com.project.MyEplPredictor.repositories.MatchRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GameweekService {

	private static final Logger log = LoggerFactory.getLogger(GameweekService.class);

	private final GameweekRepo gameweekRepo;
	private final MatchRepo matchRepo;
	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;
	private final String rapidApiKey;
	private final String bet365BaseUrl;
	private final String bet365Host;
	private final int tournamentId;
	private final DataSource dataSource;

	public GameweekService(GameweekRepo gameweekRepo,
						   MatchRepo matchRepo,
                           DataSource dataSource,
                           @Value("${bet365.api-key}") String rapidApiKey,
                           @Value("${bet365.base-url:https://bet36528.p.rapidapi.com}") String bet365BaseUrl,
                           @Value("${bet365.tournament-id:17}") int tournamentId) {
		this.gameweekRepo = gameweekRepo;
		this.matchRepo = matchRepo;
		this.dataSource = dataSource;
        // assign all final configuration fields to avoid compilation errors
        this.rapidApiKey = rapidApiKey;
        this.bet365BaseUrl = bet365BaseUrl;
        this.tournamentId = tournamentId;
        this.bet365Host = URI.create(bet365BaseUrl).getHost();
        this.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        this.httpClient = HttpClient.newHttpClient();
    }

	public GameweekResponseDto getCurrentGameweek() {
		Gameweek gameweek = resolveActiveGameweek()
				.orElseThrow(() -> new IllegalStateException("No active or upcoming gameweek configured"));
		return toDto(gameweek);
	}

	@Transactional(readOnly = true)
	public List<MatchResponseDto> getCurrentGameweekMatches() {
		Gameweek gameweek = resolveActiveGameweek()
				.orElseThrow(() -> new IllegalStateException("No active or upcoming gameweek configured"));
		List<PMatch> PMatches = matchRepo.findByGameweekIdOrderByKickoffTimeAsc(gameweek.getId());
		return PMatches.stream().map(this::toMatchDto).collect(Collectors.toList());
	}

	public GameweekResponseDto activateGameweek(Long gameweekId) {
		Gameweek target = findGameweek(gameweekId);
		if (target.getStatus() == GameweekStatus.COMPLETED) {
			throw new IllegalStateException("Completed gameweeks cannot be re-activated");
		}

		gameweekRepo.findByStatus(GameweekStatus.ACTIVE)
				.stream()
				.filter(gw -> !Objects.equals(gw.getId(), gameweekId))
				.forEach(gw -> gw.setStatus(GameweekStatus.LOCKED));

		target.setStatus(GameweekStatus.ACTIVE);
		return toDto(gameweekRepo.save(target));
	}

	public GameweekResponseDto lockGameweekIfStarted(Long gameweekId) {
		Gameweek target = findGameweek(gameweekId);
		if (target.getStatus() == GameweekStatus.LOCKED || target.getStatus() == GameweekStatus.COMPLETED) {
			return toDto(target);
		}

		Optional<PMatch> firstKickoff = matchRepo.findFirstByGameweekIdOrderByKickoffTimeAsc(gameweekId);
		if (firstKickoff.isPresent()) {
			LocalDateTime kickoff = firstKickoff.get().getKickoffTime();
			if (kickoff != null && !LocalDateTime.now().isBefore(kickoff)) {
				target.setStatus(GameweekStatus.LOCKED);
				gameweekRepo.save(target);
			}
		}
		return toDto(target);
	}

	public GameweekResponseDto completeGameweekIfFinished(Long gameweekId) {
		Gameweek target = findGameweek(gameweekId);
		boolean hasUnfinishedMatches = matchRepo.existsByGameweekIdAndStatusNot(gameweekId, MatchStatus.FINISHED);
		if (hasUnfinishedMatches) {
			throw new IllegalStateException("Gameweek still has matches in progress");
		}

		target.setStatus(GameweekStatus.COMPLETED);
		GameweekResponseDto completed = toDto(gameweekRepo.save(target));
		activateNextGameweek(target);
		return completed;
	}

	public List<MatchResponseDto> assignMatchesToGameweeks() {
		List<Gameweek> gameweeks = gameweekRepo.findAll();
		List<PMatch> PMatches = matchRepo.findAll();
		List<PMatch> updated = new ArrayList<>();

		for (PMatch PMatch : PMatches) {
			Gameweek matchWeek = PMatch.getGameweek();
			Gameweek fittingWeek = findGameweekForKickoff(PMatch.getKickoffTime(), gameweeks);
			if (fittingWeek != null && (matchWeek == null || !Objects.equals(matchWeek.getId(), fittingWeek.getId()))) {
				PMatch.setGameweek(fittingWeek);
				updated.add(PMatch);
			}
		}

		List<PMatch> persisted = updated.isEmpty() ? List.of() : matchRepo.saveAll(updated);

		return persisted.stream().map(this::toMatchDto).collect(Collectors.toList());
	}

	public List<MatchResponseDto> syncFixturesFromApi() {
		List<FixturePayload> fixtures = fetchFixturesFromApi();
		if (fixtures.isEmpty()) {
			return List.of();
		}

		List<Gameweek> gameweeks = gameweekRepo.findAll();
		List<PMatch> toPersist = new ArrayList<>();

		for (FixturePayload payload : fixtures) {
			Gameweek week = findGameweekForKickoff(payload.kickoff(), gameweeks);
			if (week == null) {
				log.warn("No gameweek found for fixture {} kickoff {}", payload.fixtureId(), payload.kickoff());
				continue;
			}

			PMatch PMatch = matchRepo.findByFixtureId(payload.fixtureId())
					.orElseGet(PMatch::new);

			if (PMatch.getId() == null) {
				PMatch.setFixtureId(payload.fixtureId());
			}

			PMatch.setHomeTeam(payload.homeTeam());
			PMatch.setAwayTeam(payload.awayTeam());
			PMatch.setKickoffTime(payload.kickoff());
			PMatch.setStatus(MatchStatus.UPCOMING);
			PMatch.setGameweek(week);
			toPersist.add(PMatch);
		}

		if (toPersist.isEmpty()) {
			return List.of();
		}

		List<PMatch> saved = matchRepo.saveAll(toPersist);
		return saved.stream().map(this::toMatchDto).collect(Collectors.toList());
	}

	private void activateNextGameweek(Gameweek completed) {
		if (completed.getStartDate() == null) {
			return;
		}

		gameweekRepo.findFirstByStatusAndStartDateAfterOrderByStartDateAsc(GameweekStatus.UPCOMING, completed.getStartDate())
				.ifPresent(next -> {
					next.setStatus(GameweekStatus.ACTIVE);
					gameweekRepo.save(next);
				});
	}

	private Optional<Gameweek> resolveActiveGameweek() {
		// look for existing active week
		Optional<Gameweek> active = gameweekRepo.findFirstByStatusOrderByStartDateAsc(GameweekStatus.ACTIVE);
		if (active.isPresent()) {
			return active;
		}

		// activate upcoming week if available
		Optional<Gameweek> upcoming = gameweekRepo.findFirstByStatusOrderByStartDateAsc(GameweekStatus.UPCOMING);
		if (upcoming.isPresent()) {
			Gameweek gw = upcoming.get();
			if (!isReadOnly()) {
				gw.setStatus(GameweekStatus.ACTIVE);
				return Optional.of(gameweekRepo.save(gw));
			} else {
				log.warn("datasource read-only, skipping activation of upcoming gameweek");
				return Optional.of(gw);
			}
		}

		// nothing configured yet - create a new gameweek for the current period
		return Optional.of(createNewGameweek());
	}

	private Gameweek findGameweek(Long id) {
		return gameweekRepo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Gameweek not found"));
	}

	// existing helper
	private Gameweek findGameweekForKickoff(LocalDateTime kickoff, List<Gameweek> gameweeks) {
		if (kickoff == null) {
			return null;
		}
		return gameweeks.stream()
				.filter(gw -> gw.getStartDate() != null && gw.getEndDate() != null)
				.filter(gw -> !kickoff.isBefore(gw.getStartDate()) && !kickoff.isAfter(gw.getEndDate()))
				.min(Comparator.comparing(Gameweek::getStartDate))
				.orElse(null);
	}

	/**
	 * Helper used to guard write operations when the configured DataSource is
	 * operating in read-only mode (for example when connected to a replica).  The
	 * method opens a connection, queries its readOnly flag and safely closes it.
	 * Any exception while acquiring the connection is treated as writable to
	 * avoid breaking normal behaviour.
	 */
	private boolean isReadOnly() {
		try (Connection conn = dataSource.getConnection()) {
			return conn.isReadOnly();
		} catch (SQLException e) {
			log.warn("unable to determine datasource read-only state, assuming writable", e);
			return false;
		}
	}

	// when no weeks exist this method will create one covering the next seven days and
	// optionally perform a fixture sync so matches are available immediately.
	private Gameweek createNewGameweek() {
		// guard write operations when running against a read-only datasource
		if (isReadOnly()) {
			throw new IllegalStateException("Database connection is read-only; cannot auto-create gameweek");
		}
		LocalDateTime now = LocalDateTime.now();
		if (isReadOnly()) {
			throw new IllegalStateException("Database connection is read-only; cannot create new gameweek");
		}
		Gameweek gw = new Gameweek();
		gw.setStartDate(now);
		gw.setEndDate(now.plusWeeks(1));

		Integer maxWeek = gameweekRepo.findMaxWeekNumber();
		int nextWeek = (maxWeek == null ? 0 : maxWeek) + 1;
		gw.setWeekNumber(nextWeek);
		gw.setSeason(now.getYear());
		gw.setStatus(GameweekStatus.ACTIVE);
		gw = gameweekRepo.save(gw);

		log.info("Created new automatic gameweek {} ({} -> {})", nextWeek, gw.getStartDate(), gw.getEndDate());

		// optionally fetch matches and assign them to the new week. this will pull all
		// fixtures and then distribute according to kickoff time; it may create many
		// matches but ensures the week isn't empty.
		try {
			syncFixturesFromApi();
			assignMatchesToGameweeks();
		} catch (Exception e) {
			log.warn("Exception while populating matches for auto-created gameweek", e);
		}

		return gw;
	}

	private GameweekResponseDto toDto(Gameweek gameweek) {
		GameweekResponseDto dto = new GameweekResponseDto();
		dto.setId(gameweek.getId());
		dto.setWeekNumber(gameweek.getWeekNumber());
		dto.setSeason(gameweek.getSeason());
		dto.setStartDate(gameweek.getStartDate());
		dto.setEndDate(gameweek.getEndDate());
		dto.setStatus(gameweek.getStatus());
		return dto;
	}

	/**
	 * Explicit creation endpoint used by admin/cron/startup flows.
	 * The returned DTO contains the newly created gameweek and the
	 * list of associated matches (snapshot of fixtures filtered by
	 * date range and capped to MAX_MATCHES).
	 */
	public CreateGameweekResponseDto createGameweek() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime end = now.plusWeeks(1);

		// build gameweek entity
		Gameweek gw = new Gameweek();
		gw.setStartDate(now);
		gw.setEndDate(end);

		Integer maxWeek = gameweekRepo.findMaxWeekNumber();
		int nextWeek = (maxWeek == null ? 0 : maxWeek) + 1;
		gw.setWeekNumber(nextWeek);
		gw.setSeason(now.getYear());
		gw.setStatus(GameweekStatus.ACTIVE);
		gw = gameweekRepo.save(gw);

		log.info("Created explicit gameweek {} ({} -> {})", nextWeek, gw.getStartDate(), gw.getEndDate());

		// fetch fixtures and pick a limited slice
		List<FixturePayload> fixtures = fetchFixturesFromApi();
		List<FixturePayload> chosen = fixtures.stream()
			.filter(f -> !f.kickoff().isBefore(now) && !f.kickoff().isAfter(end))
			.sorted(Comparator.comparing(FixturePayload::kickoff))
			.limit(20)
			.collect(Collectors.toList());

		List<PMatch> savedPMatches = new ArrayList<>();
		for (FixturePayload payload : chosen) {
			PMatch PMatch = new PMatch();
			PMatch.setFixtureId(payload.fixtureId());
			PMatch.setHomeTeam(payload.homeTeam());
			PMatch.setAwayTeam(payload.awayTeam());
			PMatch.setKickoffTime(payload.kickoff());
			PMatch.setStatus(MatchStatus.UPCOMING);
			PMatch.setGameweek(gw);
			savedPMatches.add(PMatch);
		}
		if (!savedPMatches.isEmpty()) {
			savedPMatches = matchRepo.saveAll(savedPMatches);
		}

		CreateGameweekResponseDto result = new CreateGameweekResponseDto();
		result.setGameweek(toDto(gw));
		result.setMatches(savedPMatches.stream().map(this::toMatchDto).collect(Collectors.toList()));
		return result;
	}

	private MatchResponseDto toMatchDto(PMatch PMatch) {
		MatchResponseDto dto = new MatchResponseDto();
		dto.setId(PMatch.getId());
		dto.setFixtureId(PMatch.getFixtureId());
		dto.setHomeTeam(PMatch.getHomeTeam());
		dto.setAwayTeam(PMatch.getAwayTeam());
		dto.setKickoffTime(PMatch.getKickoffTime());
		dto.setHomeScore(PMatch.getHomeScore());
		dto.setAwayScore(PMatch.getAwayScore());
		dto.setStatus(PMatch.getStatus());
		return dto;
	}

	private List<FixturePayload> fetchFixturesFromApi() {
		if (rapidApiKey == null || rapidApiKey.isBlank()) {
			log.warn("RapidAPI key missing; fixture sync skipped");
			return List.of();
		}

		log.info("syncing fixtures from API (tournamentId={})", tournamentId);
		String uri = String.format("%s/fixtures?tournamentId=%d&hasOdds=true", bet365BaseUrl, tournamentId);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(uri))
				.header("x-rapidapi-key", rapidApiKey)
				.header("x-rapidapi-host", bet365Host)
				.GET()
				.build();

		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 400) {
				log.error("Fixture sync failed with status {}", response.statusCode());
				return List.of();
			}

			JsonNode root = objectMapper.readTree(response.body());
			List<FixturePayload> fixtures = new ArrayList<>();
			if (root.isArray()) {
				for (JsonNode node : root) {
					String fixtureId = node.path("fixtureId").asText(null);
					String home = node.path("participant1Name").asText(null);
					String away = node.path("participant2Name").asText(null);
					String kickoffStr = node.path("startTime").asText(null);
					if (fixtureId == null || home == null || away == null || kickoffStr == null) {
						continue;
					}
					LocalDateTime kickoff = OffsetDateTime.parse(kickoffStr).toLocalDateTime();
					fixtures.add(new FixturePayload(fixtureId, home, away, kickoff));
				}
			}
			log.info("fixture sync returned {} entries", fixtures.size());
			return fixtures;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Fixture sync interrupted", e);
		} catch (IOException e) {
			log.error("Error parsing fixture response", e);
			return List.of();
		}
	}

	private record FixturePayload(String fixtureId, String homeTeam, String awayTeam, LocalDateTime kickoff) {
	}
}
