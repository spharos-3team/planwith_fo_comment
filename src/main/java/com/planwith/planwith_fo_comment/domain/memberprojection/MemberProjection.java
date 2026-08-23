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
		return new MemberProjection(
				memberUuid,
				normalizeNickname(nickname),
				profileImage,
				memberStatus,
				0L,
				Instant.now()
		);
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

	public boolean apply(String nickname, String profileImage, MemberStatus memberStatus, long incomingVersion) {
		if (incomingVersion > 0 && incomingVersion <= sourceVersion) {
			return false;
		}
		this.nickname = normalizeNickname(nickname);
		this.profileImage = profileImage;
		this.memberStatus = memberStatus;
		this.sourceVersion = incomingVersion > 0 ? incomingVersion : sourceVersion + 1;
		this.synchronizedAt = Instant.now();
		return true;
	}

	private static String normalizeNickname(String nickname) {
		if (nickname == null) {
			return "";
		}
		return nickname.length() <= 20 ? nickname : nickname.substring(0, 20);
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
