package com.planwith.planwith_fo_comment.adapter.out.persistence.reportprojection;

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
import lombok.Setter;

/**
 * 신고 원장/사유가 아니라 CommentReported 멱등 처리용 Inbox다.
 * 신고 사유는 Report Service가 소유한다.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(
		name = "comment_report_projection",
		indexes = {
				@Index(name = "ix_comment_report_projection_comment_uuid", columnList = "comment_uuid")
		}
)
public class CommentReportProjectionJpaEntity {

	@Id
	@Column(name = "report_uuid", columnDefinition = "char(36)", nullable = false, updatable = false)
	private UUID reportUuid;

	@Column(name = "comment_uuid", columnDefinition = "char(36)", nullable = false)
	private UUID commentUuid;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
}
