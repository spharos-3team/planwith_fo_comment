package com.planwith.planwith_fo_comment.adapter.out.persistence.comment;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.comment.CommentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
		name = "comments",
		indexes = {
				@Index(name = "ix_comments_story_uuid", columnList = "story_uuid"),
				@Index(name = "ix_comments_member_uuid", columnList = "member_uuid")
		}
)
public class CommentJpaEntity {

	@Id
	@Column(name = "comment_uuid", columnDefinition = "char(36)", nullable = false, updatable = false)
	private UUID commentUuid;

	@Column(name = "story_uuid", columnDefinition = "char(36)", nullable = false)
	private UUID storyUuid;

	@Column(name = "member_uuid", columnDefinition = "char(36)", nullable = false)
	private UUID memberUuid;

	@Column(name = "content", nullable = false, length = 2000)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private CommentStatus status;

	@Column(name = "like_count", nullable = false)
	private long likeCount;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
