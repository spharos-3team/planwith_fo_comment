package com.planwith.planwith_fo_comment.adapter.out.persistence.processedevent;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.application.command.EventMetadata;
import com.planwith.planwith_fo_comment.application.port.out.ProcessedCommentEventPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProcessedCommentEventPersistenceAdapter implements ProcessedCommentEventPort {

	private final ProcessedCommentEventJpaRepository processedCommentEventJpaRepository;

	@Override
	public boolean existsByEventUuid(UUID eventUuid) {
		return processedCommentEventJpaRepository.existsById(eventUuid);
	}

	@Override
	public Optional<Instant> findLatestOccurredAt(String targetType, UUID targetUuid) {
		return processedCommentEventJpaRepository
				.findFirstByTargetTypeAndTargetUuidOrderByOccurredAtDesc(targetType, targetUuid)
				.map(entity -> entity.getOccurredAt());
	}

	@Override
	public void save(String targetType, EventMetadata metadata) {
		processedCommentEventJpaRepository.save(ProcessedCommentEventJpaEntity.builder()
				.eventUuid(metadata.eventUuid())
				.eventType(metadata.eventType())
				.targetType(targetType)
				.targetUuid(metadata.targetUuid())
				.occurredAt(metadata.occurredAt())
				.processedAt(Instant.now())
				.build());
	}
}
