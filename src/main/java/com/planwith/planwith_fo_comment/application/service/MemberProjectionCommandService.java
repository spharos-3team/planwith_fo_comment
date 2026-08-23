package com.planwith.planwith_fo_comment.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_comment.application.command.SyncMemberProjectionCommand;
import com.planwith.planwith_fo_comment.application.port.in.SyncMemberProjectionUseCase;
import com.planwith.planwith_fo_comment.application.port.out.MemberProjectionPort;
import com.planwith.planwith_fo_comment.domain.memberprojection.MemberProjection;
import com.planwith.planwith_fo_comment.domain.memberprojection.MemberStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberProjectionCommandService implements SyncMemberProjectionUseCase {
	private static final String MEMBER_TARGET_TYPE = "MEMBER";

	private final MemberProjectionPort memberProjectionPort;
	private final ProcessedCommentEventService processedCommentEventService;

	@Override
	@Transactional
	public void sync(SyncMemberProjectionCommand command) {
		if (processedCommentEventService.isDuplicate(command.eventMetadata())) {
			return;
		}
		log.info(
				"MemberProjectionCommandService : sync : Member Projection 동기화 시작 - memberUuid={}, sourceVersion={}",
				command.memberUuid(),
				command.incomingVersion()
		);

		MemberStatus memberStatus = MemberStatus.from(command.memberStatus());
		MemberProjection projection = memberProjectionPort.findByMemberUuidForUpdate(command.memberUuid())
				.orElseGet(() -> MemberProjection.create(
						command.memberUuid(),
						command.nickname(),
						command.profileImage(),
						memberStatus
				));
		boolean applied = projection.apply(
				command.nickname(),
				command.profileImage(),
				memberStatus,
				command.incomingVersion()
		);
		if (!applied) {
			processedCommentEventService.record(MEMBER_TARGET_TYPE, command.eventMetadata());
			log.warn(
					"MemberProjectionCommandService : sync : 이전 버전 Member 이벤트 무시 - memberUuid={}, incomingVersion={}, currentVersion={}",
					command.memberUuid(),
					command.incomingVersion(),
					projection.getSourceVersion()
			);
			return;
		}
		memberProjectionPort.save(projection);
		processedCommentEventService.record(MEMBER_TARGET_TYPE, command.eventMetadata());

		log.info(
				"MemberProjectionCommandService : sync : Member Projection 동기화 완료 - memberUuid={}, sourceVersion={}",
				command.memberUuid(),
				projection.getSourceVersion()
		);
	}
}
