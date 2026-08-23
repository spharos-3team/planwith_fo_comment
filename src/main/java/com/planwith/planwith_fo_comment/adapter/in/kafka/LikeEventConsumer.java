package com.planwith.planwith_fo_comment.adapter.in.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_comment.adapter.in.kafka.event.CommentLikedEvent;
import com.planwith.planwith_fo_comment.adapter.in.kafka.event.CommentUnlikedEvent;
import com.planwith.planwith_fo_comment.application.command.EventMetadata;
import com.planwith.planwith_fo_comment.application.command.HandleLikeCommand;
import com.planwith.planwith_fo_comment.application.port.in.HandleCommentLikedUseCase;
import com.planwith.planwith_fo_comment.application.port.in.HandleCommentUnlikedUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class LikeEventConsumer {

	private final ObjectMapper objectMapper;
	private final HandleCommentLikedUseCase handleCommentLikedUseCase;
	private final HandleCommentUnlikedUseCase handleCommentUnlikedUseCase;

	@KafkaListener(topics = "${app.kafka.topics.like-created}")
	public void consumeCommentLiked(String message) {
		log.info("LikeEventConsumer : consumeCommentLiked : CommentLikedEvent 수신");
		CommentLikedEvent event = readLiked(message);
		handleCommentLikedUseCase.handleLiked(
				new HandleLikeCommand(
						event.likeUuid(),
						event.commentUuid(),
						event.memberUuid(),
						EventMetadata.validated(
								event.eventUuid(),
								event.eventType(),
								event.targetUuid(),
								event.occurredAt(),
								event.likeUuid()
						)
				)
		);
	}

	@KafkaListener(topics = "${app.kafka.topics.like-removed}")
	public void consumeCommentUnliked(String message) {
		log.info("LikeEventConsumer : consumeCommentUnliked : CommentUnlikedEvent 수신");
		CommentUnlikedEvent event = readUnliked(message);
		handleCommentUnlikedUseCase.handleUnliked(
				new HandleLikeCommand(
						event.likeUuid(),
						event.commentUuid(),
						event.memberUuid(),
						EventMetadata.validated(
								event.eventUuid(),
								event.eventType(),
								event.targetUuid(),
								event.occurredAt(),
								event.likeUuid()
						)
				)
		);
	}

	private CommentLikedEvent readLiked(String message) {
		try {
			return objectMapper.readValue(message, CommentLikedEvent.class);
		} catch (JsonProcessingException exception) {
			log.error("LikeEventConsumer : readLiked : CommentLikedEvent 역직렬화 실패");
			throw new IllegalArgumentException("CommentLikedEvent 형식이 올바르지 않습니다.", exception);
		}
	}

	private CommentUnlikedEvent readUnliked(String message) {
		try {
			return objectMapper.readValue(message, CommentUnlikedEvent.class);
		} catch (JsonProcessingException exception) {
			log.error("LikeEventConsumer : readUnliked : CommentUnlikedEvent 역직렬화 실패");
			throw new IllegalArgumentException("CommentUnlikedEvent 형식이 올바르지 않습니다.", exception);
		}
	}
}
