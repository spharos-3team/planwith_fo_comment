package com.planwith.planwith_fo_comment.adapter.out.persistence.processedevent;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
		name = "processed_comment_event",
		indexes = {
				@Index(
						name = "idx_processed_comment_event_target",
						columnList = "target_type, target_uuid, occurred_at"
				)
		}
)
public class ProcessedCommentEventJpaEntity {

	@Id
	@Column(name = "event_uuid", columnDefinition = "char(36)", nullable = false, updatable = false)
	private UUID eventUuid;

	@Column(name = "event_type", nullable = false, updatable = false, length = 50)
	private String eventType;

	@Column(name = "target_type", nullable = false, updatable = false, length = 30)
	private String targetType;

	@Column(name = "target_uuid", columnDefinition = "char(36)", nullable = false, updatable = false)
	private UUID targetUuid;

	@Column(name = "occurred_at", nullable = false, updatable = false)
	private Instant occurredAt;

	@Column(name = "processed_at", nullable = false, updatable = false)
	private Instant processedAt;
}
