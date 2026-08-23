package com.planwith.planwith_fo_comment.adapter.out.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.application.port.out.CommentEventPublisherPort;
import com.planwith.planwith_fo_comment.domain.outbox.CommentOutboxEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingCommentEventPublisher implements CommentEventPublisherPort {

	@Override
	public void publish(CommentOutboxEvent event) {
		log.info(
				"LoggingCommentEventPublisher : publish : Kafka 비활성화 상태로 Outbox 이벤트 로그만 기록 - eventType={}, commentUuid={}",
				event.getEventType(),
				event.getAggregateUuid()
		);
	}
}
