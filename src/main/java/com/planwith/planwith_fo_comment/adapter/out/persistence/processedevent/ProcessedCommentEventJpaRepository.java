package com.planwith.planwith_fo_comment.adapter.out.persistence.processedevent;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedCommentEventJpaRepository
		extends JpaRepository<ProcessedCommentEventJpaEntity, UUID> {

	Optional<ProcessedCommentEventJpaEntity> findFirstByTargetTypeAndTargetUuidOrderByOccurredAtDesc(
			String targetType,
			UUID targetUuid
	);
}
