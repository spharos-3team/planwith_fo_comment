package com.planwith.planwith_fo_comment.adapter.out.persistence.memberprojection;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "comment_member_projection")
public class CommentMemberProjectionJpaEntity {

	@Id
	@Column(name = "member_uuid", columnDefinition = "char(36)", nullable = false, updatable = false)
	private UUID memberUuid;

	@Column(name = "nickname", length = 100)
	private String nickname;

	@Column(name = "profile_image", length = 500)
	private String profileImage;

	@Column(name = "member_status", length = 30)
	private String memberStatus;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
