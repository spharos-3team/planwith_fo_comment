package com.planwith.planwith_fo_comment.adapter.out.persistence.storyprojection;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.storyprojection.StoryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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
@Table(name = "comment_story_projection")
public class CommentStoryProjectionJpaEntity {

	@Id
	@Column(name = "story_uuid", columnDefinition = "char(36)", nullable = false, updatable = false)
	private UUID storyUuid;

	@Column(name = "owner_member_uuid", columnDefinition = "char(36)")
	private UUID ownerMemberUuid;

	@Column(name = "comment_enabled", nullable = false)
	private boolean commentEnabled;

	@Enumerated(EnumType.STRING)
	@Column(name = "story_status", nullable = false, length = 20)
	private StoryStatus storyStatus;

	@Column(name = "source_version", nullable = false)
	private long sourceVersion;

	@Column(name = "synchronized_at", nullable = false)
	private Instant synchronizedAt;
}
