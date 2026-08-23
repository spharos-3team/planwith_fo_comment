package com.planwith.planwith_fo_comment.adapter.in.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_comment.adapter.in.kafka.event.CommentReportedEvent;
import com.planwith.planwith_fo_comment.application.command.HandleReportCommand;
import com.planwith.planwith_fo_comment.application.port.in.HandleCommentReportedUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class ReportEventConsumer {

	private final ObjectMapper objectMapper;
	private final HandleCommentReportedUseCase handleCommentReportedUseCase;

	@KafkaListener(topics = "${app.kafka.topics.report-created}")
	public void consumeCommentReported(String message) {
		log.info("ReportEventConsumer : consumeCommentReported : CommentReportedEvent 수신");
		CommentReportedEvent event = read(message);
		handleCommentReportedUseCase.handleReported(
				new HandleReportCommand(event.reportUuid(), event.commentUuid(), event.memberUuid())
		);
	}

	private CommentReportedEvent read(String message) {
		try {
			return objectMapper.readValue(message, CommentReportedEvent.class);
		} catch (JsonProcessingException exception) {
			log.error("ReportEventConsumer : read : CommentReportedEvent 역직렬화 실패");
			throw new IllegalArgumentException("CommentReportedEvent 형식이 올바르지 않습니다.", exception);
		}
	}
}
