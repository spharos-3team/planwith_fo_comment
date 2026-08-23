package com.planwith.planwith_fo_comment.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_comment.application.command.EventMetadata;

public interface ProcessedCommentEventPort {

	boolean existsByEventUuid(UUID eventUuid);

	Optional<Instant> findLatestOccurredAt(String targetType, UUID targetUuid);

	void save(String targetType, EventMetadata metadata);
}
