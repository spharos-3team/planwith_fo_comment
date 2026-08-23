package com.planwith.planwith_fo_comment.domain.outbox;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.comment.CommentEventType;

public class CommentOutboxEvent {

	private final UUID outboxUuid;
	private final CommentEventType eventType;
	private final UUID aggregateUuid;
	private final String payload;
	private OutboxStatus status;
	private final Instant createdAt;
	private Instant publishedAt;

	private CommentOutboxEvent(
			UUID outboxUuid,
			CommentEventType eventType,
			UUID aggregateUuid,
			String payload,
			OutboxStatus status,
			Instant createdAt,
			Instant publishedAt
	) {
		this.outboxUuid = outboxUuid;
		this.eventType = eventType;
		this.aggregateUuid = aggregateUuid;
		this.payload = payload;
		this.status = status;
		this.createdAt = createdAt;
		this.publishedAt = publishedAt;
	}

	public static CommentOutboxEvent pending(
			CommentEventType eventType,
			UUID aggregateUuid,
			String payload
	) {
		return new CommentOutboxEvent(
				UUID.randomUUID(),
				eventType,
				aggregateUuid,
				payload,
				OutboxStatus.PENDING,
				Instant.now(),
				null
		);
	}

	public static CommentOutboxEvent restore(
			UUID outboxUuid,
			CommentEventType eventType,
			UUID aggregateUuid,
			String payload,
			OutboxStatus status,
			Instant createdAt,
			Instant publishedAt
	) {
		return new CommentOutboxEvent(
				outboxUuid,
				eventType,
				aggregateUuid,
				payload,
				status,
				createdAt,
				publishedAt
		);
	}

	public void markPublished() {
		this.status = OutboxStatus.PUBLISHED;
		this.publishedAt = Instant.now();
	}

	public boolean isPending() {
		return status == OutboxStatus.PENDING;
	}

	public UUID getOutboxUuid() {
		return outboxUuid;
	}

	public CommentEventType getEventType() {
		return eventType;
	}

	public UUID getAggregateUuid() {
		return aggregateUuid;
	}

	public String getPayload() {
		return payload;
	}

	public OutboxStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}
}
