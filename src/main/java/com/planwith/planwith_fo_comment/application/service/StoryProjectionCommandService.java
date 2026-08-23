package com.planwith.planwith_fo_comment.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_comment.application.command.MarkStoryDeletedCommand;
import com.planwith.planwith_fo_comment.application.command.SyncStoryProjectionCommand;
import com.planwith.planwith_fo_comment.application.port.in.MarkStoryDeletedUseCase;
import com.planwith.planwith_fo_comment.application.port.in.SyncStoryProjectionUseCase;
import com.planwith.planwith_fo_comment.application.port.out.StoryProjectionPort;
import com.planwith.planwith_fo_comment.domain.storyprojection.StoryProjection;
import com.planwith.planwith_fo_comment.domain.storyprojection.StoryStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryProjectionCommandService implements SyncStoryProjectionUseCase, MarkStoryDeletedUseCase {

	private final StoryProjectionPort storyProjectionPort;

	@Override
	@Transactional
	public void sync(SyncStoryProjectionCommand command) {
		log.info(
				"StoryProjectionCommandService : sync : Story Projection 동기화 시작 - storyUuid={}, sourceVersion={}",
				command.storyUuid(),
				command.incomingVersion()
		);

		StoryStatus storyStatus = StoryStatus.from(command.storyStatus());
		StoryProjection projection = storyProjectionPort.findByStoryUuid(command.storyUuid())
				.orElseGet(() -> StoryProjection.create(
						command.storyUuid(),
						command.ownerMemberUuid(),
						command.commentEnabled(),
						storyStatus
				));
		boolean applied = projection.apply(
				command.ownerMemberUuid(),
				command.commentEnabled(),
				storyStatus,
				command.incomingVersion()
		);
		if (!applied) {
			log.warn(
					"StoryProjectionCommandService : sync : 이전 버전 Story 이벤트 무시 - storyUuid={}, incomingVersion={}, currentVersion={}",
					command.storyUuid(),
					command.incomingVersion(),
					projection.getSourceVersion()
			);
			return;
		}
		storyProjectionPort.save(projection);

		log.info(
				"StoryProjectionCommandService : sync : Story Projection 동기화 완료 - storyUuid={}, ownerMemberUuid={}",
				command.storyUuid(),
				command.ownerMemberUuid()
		);
	}

	@Override
	@Transactional
	public void markDeleted(MarkStoryDeletedCommand command) {
		log.info(
				"StoryProjectionCommandService : markDeleted : StoryDeleted 반영 시작 - storyUuid={}",
				command.storyUuid()
		);

		StoryProjection projection = storyProjectionPort.findByStoryUuid(command.storyUuid())
				.orElseGet(() -> StoryProjection.create(
						command.storyUuid(),
						null,
						false,
						StoryStatus.DELETED
				));
		boolean applied = projection.markDeleted(command.incomingVersion());
		if (!applied) {
			log.warn(
					"StoryProjectionCommandService : markDeleted : 이전 버전 StoryDeleted 무시 - storyUuid={}",
					command.storyUuid()
			);
			return;
		}
		storyProjectionPort.save(projection);

		log.info(
				"StoryProjectionCommandService : markDeleted : StoryDeleted 반영 완료 - storyUuid={}",
				command.storyUuid()
		);
	}
}
