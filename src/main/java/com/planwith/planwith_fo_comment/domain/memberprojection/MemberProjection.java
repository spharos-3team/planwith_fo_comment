package com.planwith.planwith_fo_comment.domain.memberprojection;

import java.time.Instant;
import java.util.UUID;

public class MemberProjection {

	private final UUID memberUuid;
	private String nickname;
	private String profileImage;
	private String memberStatus;
	private Instant updatedAt;

	private MemberProjection(
			UUID memberUuid,
			String nickname,
			String profileImage,
			String memberStatus,
			Instant updatedAt
	) {
		this.memberUuid = memberUuid;
		this.nickname = nickname;
		this.profileImage = profileImage;
		this.memberStatus = memberStatus;
		this.updatedAt = updatedAt;
	}

	public static MemberProjection create(
			UUID memberUuid,
			String nickname,
			String profileImage,
			String memberStatus
	) {
		return new MemberProjection(memberUuid, nickname, profileImage, memberStatus, Instant.now());
	}

	public static MemberProjection restore(
			UUID memberUuid,
			String nickname,
			String profileImage,
			String memberStatus,
			Instant updatedAt
	) {
		return new MemberProjection(memberUuid, nickname, profileImage, memberStatus, updatedAt);
	}

	public void sync(String nickname, String profileImage, String memberStatus) {
		this.nickname = nickname;
		this.profileImage = profileImage;
		this.memberStatus = memberStatus;
		this.updatedAt = Instant.now();
	}

	public UUID getMemberUuid() {
		return memberUuid;
	}

	public String getNickname() {
		return nickname;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public String getMemberStatus() {
		return memberStatus;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
