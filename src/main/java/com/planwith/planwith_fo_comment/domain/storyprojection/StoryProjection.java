package com.planwith.planwith_fo_comment.domain.storyprojection;

import java.time.Instant;
import java.util.UUID;

public class StoryProjection {

	private final UUID storyUuid;
	private UUID ownerMemberUuid;
	private boolean commentEnabled;
	private StoryStatus storyStatus;
	private long sourceVersion;
	private Instant synchronizedAt;

	private StoryProjection(
			UUID storyUuid,
			UUID ownerMemberUuid,
			boolean commentEnabled,
			StoryStatus storyStatus,
			long sourceVersion,
			Instant synchronizedAt
	) {
		this.storyUuid = storyUuid;
		this.ownerMemberUuid = ownerMemberUuid;
		this.commentEnabled = commentEnabled;
		this.storyStatus = storyStatus;
		this.sourceVersion = sourceVersion;
		this.synchronizedAt = synchronizedAt;
	}

	public static StoryProjection create(
			UUID storyUuid,
			UUID ownerMemberUuid,
			boolean commentEnabled,
			StoryStatus storyStatus
	) {
		return new StoryProjection(
				storyUuid,
				ownerMemberUuid,
				commentEnabled,
				storyStatus,
				0L,
				Instant.now()
		);
	}

	public static StoryProjection restore(
			UUID storyUuid,
			UUID ownerMemberUuid,
			boolean commentEnabled,
			StoryStatus storyStatus,
			long sourceVersion,
			Instant synchronizedAt
	) {
		return new StoryProjection(
				storyUuid,
				ownerMemberUuid,
				commentEnabled,
				storyStatus,
				sourceVersion,
				synchronizedAt
		);
	}

	public boolean apply(
			UUID ownerMemberUuid,
			boolean commentEnabled,
			StoryStatus storyStatus,
			long incomingVersion
	) {
		if (isStale(incomingVersion)) {
			return false;
		}
		this.ownerMemberUuid = ownerMemberUuid;
		this.commentEnabled = commentEnabled;
		this.storyStatus = storyStatus;
		this.sourceVersion = resolveVersion(incomingVersion);
		this.synchronizedAt = Instant.now();
		return true;
	}

	public boolean markDeleted(long incomingVersion) {
		if (isStale(incomingVersion)) {
			return false;
		}
		this.commentEnabled = false;
		this.storyStatus = StoryStatus.DELETED;
		this.sourceVersion = resolveVersion(incomingVersion);
		this.synchronizedAt = Instant.now();
		return true;
	}

	private boolean isStale(long incomingVersion) {
		return incomingVersion > 0 && incomingVersion <= sourceVersion;
	}

	private long resolveVersion(long incomingVersion) {
		return incomingVersion > 0 ? incomingVersion : sourceVersion + 1;
	}

	public UUID getStoryUuid() {
		return storyUuid;
	}

	public UUID getOwnerMemberUuid() {
		return ownerMemberUuid;
	}

	public boolean isCommentEnabled() {
		return commentEnabled;
	}

	public StoryStatus getStoryStatus() {
		return storyStatus;
	}

	public long getSourceVersion() {
		return sourceVersion;
	}

	public Instant getSynchronizedAt() {
		return synchronizedAt;
	}
}
