package com.planwith.planwith_fo_comment.application.service;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.application.command.EventMetadata;
import com.planwith.planwith_fo_comment.application.port.out.ProcessedCommentEventPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProcessedCommentEventService {

	private final ProcessedCommentEventPort processedCommentEventPort;

	public boolean isDuplicate(EventMetadata metadata) {
		return metadata != null && processedCommentEventPort.existsByEventUuid(metadata.eventUuid());
	}

	public boolean isOlderThanLatest(String targetType, EventMetadata metadata) {
		if (metadata == null) {
			return false;
		}
		return processedCommentEventPort.findLatestOccurredAt(targetType, metadata.targetUuid())
				.map(latest -> !metadata.occurredAt().isAfter(latest))
				.orElse(false);
	}

	public void record(String targetType, EventMetadata metadata) {
		if (metadata != null) {
			processedCommentEventPort.save(targetType, metadata);
		}
	}
}
