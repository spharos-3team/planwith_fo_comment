package com.planwith.planwith_fo_comment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicProperties(
		String memberChanged,
		String storyCreated,
		String storyUpdated,
		String storyDeleted,
		String likeCreated,
		String likeRemoved,
		String reportCreated,
		String commentCreated,
		String commentUpdated,
		String commentDeleted
) {
}
