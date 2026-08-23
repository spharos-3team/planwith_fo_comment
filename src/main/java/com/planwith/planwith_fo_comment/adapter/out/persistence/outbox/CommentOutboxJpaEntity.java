package com.planwith.planwith_fo_comment.adapter.out.persistence.outbox;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.comment.CommentEventType;
import com.planwith.planwith_fo_comment.domain.outbox.OutboxStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(
		name = "comment_outbox",
		indexes = {
				@Index(name = "ix_comment_outbox_status_created_at", columnList = "status, created_at")
		}
)
public class CommentOutboxJpaEntity {

	@Id
	@Column(name = "outbox_uuid", columnDefinition = "char(36)", nullable = false, updatable = false)
	private UUID outboxUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 40)
	private CommentEventType eventType;

	@Column(name = "aggregate_uuid", columnDefinition = "char(36)", nullable = false)
	private UUID aggregateUuid;

	@Lob
	@Column(name = "payload", nullable = false)
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private OutboxStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "published_at")
	private Instant publishedAt;
}
