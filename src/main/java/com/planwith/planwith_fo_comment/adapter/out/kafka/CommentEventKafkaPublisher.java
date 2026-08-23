package com.planwith.planwith_fo_comment.adapter.out.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.application.port.out.CommentEventPublisherPort;
import com.planwith.planwith_fo_comment.config.KafkaTopicProperties;
import com.planwith.planwith_fo_comment.domain.comment.CommentEventType;
import com.planwith.planwith_fo_comment.domain.outbox.CommentOutboxEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class CommentEventKafkaPublisher implements CommentEventPublisherPort {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final KafkaTopicProperties kafkaTopicProperties;

	@Override
	public void publish(CommentOutboxEvent event) {
		String topic = resolveTopic(event.getEventType());
		log.info(
				"CommentEventKafkaPublisher : publish : Comment 이벤트 발행 시작 - topic={}, commentUuid={}",
				topic,
				event.getAggregateUuid()
		);
		kafkaTemplate.send(topic, event.getAggregateUuid().toString(), event.getPayload());
		log.info(
				"CommentEventKafkaPublisher : publish : Comment 이벤트 발행 완료 - topic={}, commentUuid={}",
				topic,
				event.getAggregateUuid()
		);
	}

	private String resolveTopic(CommentEventType eventType) {
		return switch (eventType) {
			case COMMENT_CREATED -> kafkaTopicProperties.commentCreated();
			case COMMENT_UPDATED -> kafkaTopicProperties.commentUpdated();
			case COMMENT_DELETED -> kafkaTopicProperties.commentDeleted();
		};
	}
}
