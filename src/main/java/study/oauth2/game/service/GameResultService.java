package study.oauth2.game.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import study.oauth2.game.domain.dto.GameResultRequestDto;
import study.oauth2.game.domain.dto.RecentResultDto;
import study.oauth2.game.domain.dto.ResultPagingDto;
import study.oauth2.game.domain.dto.TotalResultDto;
import study.oauth2.game.domain.entity.GameResult;
import study.oauth2.game.repository.GameResultRepository;
import study.oauth2.game.repository.HighlightRepository;
import study.oauth2.user.domain.entity.User;
import study.oauth2.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameResultService {

	private final GameResultRepository gameResultRepository;
	private final HighlightRepository highlightRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	public RecentResultDto saveGameResult(String userEmail, GameResultRequestDto gameResultRequestDto) throws
		JsonProcessingException {

		User user = userRepository.findByEmailWithProfileDefault(userEmail);
		User opponent = userRepository.findByEmailWithProfileDefault(gameResultRequestDto.getOpponentEmail());
		GameResult gameResult = GameResultRequestDto.toEntity(user, opponent, gameResultRequestDto);

		// Highlight highlight = GameResultRequestDto.toHighlightEntity(gameResultRequestDto, objectMapper);
		// highlightRepository.save(highlight);
		gameResultRepository.save(gameResult);
		// TODO: Highlight 좌표 결과 객체 -> JSON 문자열로 변환하여 저장
		// return GameResultResponseDto.of(gameResult.getTotalDamage(), gameResult.getHighlight().getId());
		return RecentResultDto.toDto(gameResult);
	}

	public Page<RecentResultDto> getGameResult(ResultPagingDto resultPagingDto) {
		Pageable pageable = PageRequest.of(resultPagingDto.getPage(), resultPagingDto.getSize(),
			Sort.by(convertToSortDirection(resultPagingDto.getSort()), resultPagingDto.getSortField()));
		Page<GameResult> gameResults = gameResultRepository.findAllGameResultPage(pageable, resultPagingDto);
		return gameResults.map(RecentResultDto::toDto);
	}

	private Sort.Direction convertToSortDirection(String sort) {
		if (sort.equalsIgnoreCase("ASC")) {
			return Sort.Direction.ASC;
		} else if (sort.equalsIgnoreCase("DESC")) {
			return Sort.Direction.DESC;
		} else {
			throw new IllegalArgumentException("Invalid sort direction: " + sort);
		}
	}

	public TotalResultDto countGameResult(String userEmail) {
		Long totalWin = gameResultRepository.countWinByUserEmail(userEmail);
		Long totalLose = gameResultRepository.countLoseByUserEmail(userEmail);
		return TotalResultDto.create(totalWin, totalLose);
	}
}
