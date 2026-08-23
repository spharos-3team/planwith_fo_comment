package com.planwith.planwith_fo_comment.adapter.out.persistence.memberprojection;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.memberprojection.MemberStatus;

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
@Table(name = "comment_member_projection")
public class CommentMemberProjectionJpaEntity {

	@Id
	@Column(name = "member_uuid", columnDefinition = "char(36)", nullable = false, updatable = false)
	private UUID memberUuid;

	@Column(name = "nickname", nullable = false, length = 20)
	private String nickname;

	@Column(name = "profile_image", length = 1000)
	private String profileImage;

	@Enumerated(EnumType.STRING)
	@Column(name = "member_status", nullable = false, length = 20)
	private MemberStatus memberStatus;

	@Column(name = "source_version", nullable = false)
	private long sourceVersion;

	@Column(name = "synchronized_at", nullable = false)
	private Instant synchronizedAt;
}
