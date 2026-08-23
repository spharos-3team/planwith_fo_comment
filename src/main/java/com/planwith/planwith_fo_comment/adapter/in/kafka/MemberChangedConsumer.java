package com.planwith.planwith_fo_comment.adapter.in.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_comment.adapter.in.kafka.event.MemberChangedEvent;
import com.planwith.planwith_fo_comment.application.command.EventMetadata;
import com.planwith.planwith_fo_comment.application.command.SyncMemberProjectionCommand;
import com.planwith.planwith_fo_comment.application.port.in.SyncMemberProjectionUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class MemberChangedConsumer {

	private final ObjectMapper objectMapper;
	private final SyncMemberProjectionUseCase syncMemberProjectionUseCase;

	@KafkaListener(topics = "${app.kafka.topics.member-changed}")
	public void consume(String message) {
		log.info("MemberChangedConsumer : consume : MemberChanged 이벤트 수신");
		MemberChangedEvent event = read(message);
		syncMemberProjectionUseCase.sync(new SyncMemberProjectionCommand(
				event.memberUuid(),
				event.nickname(),
				event.profileImage(),
				event.memberStatus(),
				event.sourceVersion(),
				EventMetadata.validatedVersioned(
						event.eventUuid(),
						event.eventType(),
						event.targetUuid(),
						event.occurredAt(),
						event.memberUuid(),
						event.sourceVersion()
				)
		));
	}

	private MemberChangedEvent read(String message) {
		try {
			return objectMapper.readValue(message, MemberChangedEvent.class);
		} catch (JsonProcessingException exception) {
			log.error("MemberChangedConsumer : consume : MemberChanged 이벤트 역직렬화 실패");
			throw new IllegalArgumentException("MemberChanged 이벤트 형식이 올바르지 않습니다.", exception);
		}
	}
}
