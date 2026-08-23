package com.planwith.planwith_fo_comment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicProperties(
		String memberProfileChanged,
		String likeCreated,
		String likeRemoved,
		String commentCreated,
		String commentUpdated,
		String commentDeleted
) {
}
