package com.planwith.planwith_fo_comment.adapter.out.persistence.comment;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.comment.ModerationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
		name = "story_comment",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_story_comment_uuid", columnNames = "comment_uuid")
		},
		indexes = {
				@Index(name = "idx_story_comment_story", columnList = "story_uuid, deleted_at, created_at"),
				@Index(name = "idx_story_comment_member", columnList = "member_uuid, deleted_at"),
				@Index(name = "idx_story_comment_parent", columnList = "story_uuid, parent_comment_uuid, deleted_at, created_at"),
				@Index(name = "idx_story_comment_moderation", columnList = "story_uuid, moderation_status, report_count")
		}
)
public class StoryCommentJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_id", nullable = false, updatable = false)
	private Long commentId;

	@Column(name = "comment_uuid", columnDefinition = "char(36)", nullable = false, updatable = false)
	private UUID commentUuid;

	@Column(name = "story_uuid", columnDefinition = "char(36)", nullable = false)
	private UUID storyUuid;

	@Column(name = "member_uuid", columnDefinition = "char(36)", nullable = false)
	private UUID memberUuid;

	@Column(name = "parent_comment_uuid", columnDefinition = "char(36)")
	private UUID parentCommentUuid;

	@Column(name = "comment_content", nullable = false, length = 1000)
	private String commentContent;

	@Column(name = "comment_like_count", nullable = false)
	private long commentLikeCount;

	@Column(name = "report_count", nullable = false)
	private long reportCount;

	@Enumerated(EnumType.STRING)
	@Column(name = "moderation_status", nullable = false, length = 20)
	private ModerationStatus moderationStatus;

	@Column(name = "hidden_at")
	private Instant hiddenAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;
}
