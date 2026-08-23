package com.planwith.planwith_fo_comment.domain.memberprojection;

import java.time.Instant;
import java.util.UUID;

public class MemberProjection {

	private final UUID memberUuid;
	private String nickname;
	private String profileImage;
	private MemberStatus memberStatus;
	private long sourceVersion;
	private Instant synchronizedAt;

	private MemberProjection(
			UUID memberUuid,
			String nickname,
			String profileImage,
			MemberStatus memberStatus,
			long sourceVersion,
			Instant synchronizedAt
	) {
		this.memberUuid = memberUuid;
		this.nickname = nickname;
		this.profileImage = profileImage;
		this.memberStatus = memberStatus;
		this.sourceVersion = sourceVersion;
		this.synchronizedAt = synchronizedAt;
	}

	public static MemberProjection create(
			UUID memberUuid,
			String nickname,
			String profileImage,
			MemberStatus memberStatus
	) {
		return new MemberProjection(memberUuid, nickname, profileImage, memberStatus, 0L, Instant.now());
	}

	public static MemberProjection restore(
			UUID memberUuid,
			String nickname,
			String profileImage,
			MemberStatus memberStatus,
			long sourceVersion,
			Instant synchronizedAt
	) {
		return new MemberProjection(
				memberUuid,
				nickname,
				profileImage,
				memberStatus,
				sourceVersion,
				synchronizedAt
		);
	}

	public void sync(String nickname, String profileImage, MemberStatus memberStatus) {
		this.nickname = nickname;
		this.profileImage = profileImage;
		this.memberStatus = memberStatus;
		this.sourceVersion += 1;
		this.synchronizedAt = Instant.now();
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

	public MemberStatus getMemberStatus() {
		return memberStatus;
	}

	public long getSourceVersion() {
		return sourceVersion;
	}

	public Instant getSynchronizedAt() {
		return synchronizedAt;
	}
}
