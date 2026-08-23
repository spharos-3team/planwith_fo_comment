package com.planwith.planwith_fo_comment.adapter.in.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_comment.adapter.in.kafka.event.LikeChangedEvent;
import com.planwith.planwith_fo_comment.application.command.HandleLikeCommand;
import com.planwith.planwith_fo_comment.application.port.in.HandleLikeCreatedUseCase;
import com.planwith.planwith_fo_comment.application.port.in.HandleLikeRemovedUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class LikeEventConsumer {

	private final ObjectMapper objectMapper;
	private final HandleLikeCreatedUseCase handleLikeCreatedUseCase;
	private final HandleLikeRemovedUseCase handleLikeRemovedUseCase;

	@KafkaListener(topics = "${app.kafka.topics.like-created}")
	public void consumeLikeCreated(String message) {
		log.info("LikeEventConsumer : consumeLikeCreated : LikeCreated 이벤트 수신");
		LikeChangedEvent event = read(message);
		handleLikeCreatedUseCase.handleCreated(toCommand(event));
	}

	@KafkaListener(topics = "${app.kafka.topics.like-removed}")
	public void consumeLikeRemoved(String message) {
		log.info("LikeEventConsumer : consumeLikeRemoved : LikeRemoved 이벤트 수신");
		LikeChangedEvent event = read(message);
		handleLikeRemovedUseCase.handleRemoved(toCommand(event));
	}

	private HandleLikeCommand toCommand(LikeChangedEvent event) {
		return new HandleLikeCommand(event.likeUuid(), event.commentUuid(), event.memberUuid());
	}

	private LikeChangedEvent read(String message) {
		try {
			return objectMapper.readValue(message, LikeChangedEvent.class);
		} catch (JsonProcessingException exception) {
			log.error("LikeEventConsumer : read : Like 이벤트 역직렬화 실패");
			throw new IllegalArgumentException("Like 이벤트 형식이 올바르지 않습니다.", exception);
		}
	}
}
