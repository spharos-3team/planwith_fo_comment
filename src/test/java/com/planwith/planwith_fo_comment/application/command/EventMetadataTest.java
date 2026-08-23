package com.planwith.planwith_fo_comment.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class EventMetadataTest {

	private static final UUID EVENT_UUID = UUID.randomUUID();
	private static final UUID TARGET_UUID = UUID.randomUUID();
	private static final Instant OCCURRED_AT = Instant.parse("2026-08-24T00:00:00Z");

	@Test
	void validatesRequiredEventMetadata() {
		EventMetadata metadata = EventMetadata.validated(
				EVENT_UUID,
				"COMMENT_LIKED",
				TARGET_UUID,
				OCCURRED_AT,
				TARGET_UUID
		);

		assertThat(metadata.eventUuid()).isEqualTo(EVENT_UUID);
		assertThat(metadata.targetUuid()).isEqualTo(TARGET_UUID);
	}

	@Test
	void rejectsMissingEventUuid() {
		assertThatIllegalArgumentException().isThrownBy(() -> EventMetadata.validated(
				null,
				"COMMENT_LIKED",
				TARGET_UUID,
				OCCURRED_AT,
				TARGET_UUID
		));
	}

	@Test
	void rejectsMismatchedTargetUuid() {
		assertThatIllegalArgumentException().isThrownBy(() -> EventMetadata.validated(
				EVENT_UUID,
				"COMMENT_LIKED",
				UUID.randomUUID(),
				OCCURRED_AT,
				TARGET_UUID
		));
	}

	@Test
	void rejectsMissingProjectionVersion() {
		assertThatIllegalArgumentException().isThrownBy(() -> EventMetadata.validatedVersioned(
				EVENT_UUID,
				"MEMBER_CHANGED",
				TARGET_UUID,
				OCCURRED_AT,
				TARGET_UUID,
				null
		));
	}
}
