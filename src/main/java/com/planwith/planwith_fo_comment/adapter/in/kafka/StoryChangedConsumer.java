package com.planwith.planwith_fo_comment.adapter.in.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_comment.adapter.in.kafka.event.StoryChangedEvent;
import com.planwith.planwith_fo_comment.adapter.in.kafka.event.StoryDeletedEvent;
import com.planwith.planwith_fo_comment.application.command.MarkStoryDeletedCommand;
import com.planwith.planwith_fo_comment.application.command.SyncStoryProjectionCommand;
import com.planwith.planwith_fo_comment.application.port.in.MarkStoryDeletedUseCase;
import com.planwith.planwith_fo_comment.application.port.in.SyncStoryProjectionUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class StoryChangedConsumer {

	private final ObjectMapper objectMapper;
	private final SyncStoryProjectionUseCase syncStoryProjectionUseCase;
	private final MarkStoryDeletedUseCase markStoryDeletedUseCase;

	@KafkaListener(topics = "${app.kafka.topics.story-created}")
	public void consumeCreated(String message) {
		log.info("StoryChangedConsumer : consumeCreated : StoryCreated 이벤트 수신");
		sync(readChanged(message));
	}

	@KafkaListener(topics = "${app.kafka.topics.story-updated}")
	public void consumeUpdated(String message) {
		log.info("StoryChangedConsumer : consumeUpdated : StoryUpdated 이벤트 수신");
		sync(readChanged(message));
	}

	@KafkaListener(topics = "${app.kafka.topics.story-deleted}")
	public void consumeDeleted(String message) {
		log.info("StoryChangedConsumer : consumeDeleted : StoryDeleted 이벤트 수신");
		StoryDeletedEvent event = readDeleted(message);
		markStoryDeletedUseCase.markDeleted(new MarkStoryDeletedCommand(event.storyUuid(), event.sourceVersion()));
	}

	private void sync(StoryChangedEvent event) {
		syncStoryProjectionUseCase.sync(new SyncStoryProjectionCommand(
				event.storyUuid(),
				event.ownerMemberUuid(),
				event.commentEnabled() == null || event.commentEnabled(),
				event.storyStatus(),
				event.sourceVersion()
		));
	}

	private StoryChangedEvent readChanged(String message) {
		try {
			return objectMapper.readValue(message, StoryChangedEvent.class);
		} catch (JsonProcessingException exception) {
			log.error("StoryChangedConsumer : readChanged : Story 이벤트 역직렬화 실패");
			throw new IllegalArgumentException("Story 이벤트 형식이 올바르지 않습니다.", exception);
		}
	}

	private StoryDeletedEvent readDeleted(String message) {
		try {
			return objectMapper.readValue(message, StoryDeletedEvent.class);
		} catch (JsonProcessingException exception) {
			log.error("StoryChangedConsumer : readDeleted : StoryDeleted 이벤트 역직렬화 실패");
			throw new IllegalArgumentException("StoryDeleted 이벤트 형식이 올바르지 않습니다.", exception);
		}
	}
}
