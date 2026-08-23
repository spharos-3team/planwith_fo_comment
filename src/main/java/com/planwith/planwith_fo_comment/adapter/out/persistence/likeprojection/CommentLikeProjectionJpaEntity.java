package com.planwith.planwith_fo_comment.adapter.out.persistence.likeprojection;

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
 * Like 원장이 아니라 CommentLiked/Unliked 멱등 처리용 Inbox다.
 * 누가 좋아요를 눌렀는지는 Like Service가 소유한다.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(
		name = "comment_like_projection",
		indexes = {
				@Index(name = "ix_comment_like_projection_comment_uuid", columnList = "comment_uuid")
		}
)
public class CommentLikeProjectionJpaEntity {

	@Id
	@Column(name = "like_uuid", columnDefinition = "char(36)", nullable = false, updatable = false)
	private UUID likeUuid;

	@Column(name = "comment_uuid", columnDefinition = "char(36)", nullable = false)
	private UUID commentUuid;

	@Column(name = "member_uuid", columnDefinition = "char(36)", nullable = false)
	private UUID memberUuid;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
}
