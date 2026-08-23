package com.planwith.planwith_fo_comment.adapter.out.persistence.outbox;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_comment.application.port.out.CommentOutboxPort;
import com.planwith.planwith_fo_comment.domain.comment.StoryComment;
import com.planwith.planwith_fo_comment.domain.comment.CommentEventType;
import com.planwith.planwith_fo_comment.domain.outbox.CommentOutboxEvent;
import com.planwith.planwith_fo_comment.domain.outbox.OutboxStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentOutboxPersistenceAdapter implements CommentOutboxPort {

	private final CommentOutboxJpaRepository commentOutboxJpaRepository;
	private final ObjectMapper objectMapper;

	@Override
	public void saveCommentCreated(StoryComment comment) {
		save(comment, CommentEventType.COMMENT_CREATED);
	}

	@Override
	public void saveCommentUpdated(StoryComment comment) {
		save(comment, CommentEventType.COMMENT_UPDATED);
	}

	@Override
	public void saveCommentDeleted(StoryComment comment) {
		save(comment, CommentEventType.COMMENT_DELETED);
	}

	@Override
	public List<CommentOutboxEvent> findPending(int limit) {
		return commentOutboxJpaRepository
				.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, PageRequest.of(0, limit))
				.stream()
				.map(this::toDomain)
				.toList();
	}

	@Override
	public void markPublished(UUID outboxUuid) {
		CommentOutboxJpaEntity entity = commentOutboxJpaRepository.findById(outboxUuid)
				.orElseThrow(() -> new IllegalStateException("Outbox 이벤트를 찾을 수 없습니다. outboxUuid=" + outboxUuid));
		CommentOutboxEvent event = toDomain(entity);
		event.markPublished();
		entity.setStatus(event.getStatus());
		entity.setPublishedAt(event.getPublishedAt());
		commentOutboxJpaRepository.save(entity);
	}

	private void save(StoryComment comment, CommentEventType eventType) {
		CommentOutboxEvent event = CommentOutboxEvent.pending(
				eventType,
				comment.getCommentUuid(),
				toPayload(comment, eventType)
		);
		commentOutboxJpaRepository.save(CommentOutboxJpaEntity.builder()
				.outboxUuid(event.getOutboxUuid())
				.eventType(event.getEventType())
				.aggregateUuid(event.getAggregateUuid())
				.payload(event.getPayload())
				.status(event.getStatus())
				.createdAt(event.getCreatedAt())
				.publishedAt(event.getPublishedAt())
				.build());
	}

	private String toPayload(StoryComment comment, CommentEventType eventType) {
		CommentOutboxPayload payload = new CommentOutboxPayload(
				eventType.name(),
				comment.getCommentUuid(),
				comment.getStoryUuid(),
				comment.getMemberUuid(),
				comment.getUpdatedAt()
		);
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Comment Outbox payload 직렬화에 실패했습니다.", exception);
		}
	}

	private CommentOutboxEvent toDomain(CommentOutboxJpaEntity entity) {
		return CommentOutboxEvent.restore(
				entity.getOutboxUuid(),
				entity.getEventType(),
				entity.getAggregateUuid(),
				entity.getPayload(),
				entity.getStatus(),
				entity.getCreatedAt(),
				entity.getPublishedAt()
		);
	}

	public record CommentOutboxPayload(
			String eventType,
			UUID commentUuid,
			UUID storyUuid,
			UUID memberUuid,
			java.time.Instant occurredAt
	) {
	}
}
