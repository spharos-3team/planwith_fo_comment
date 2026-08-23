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

	private final MemberProjectionPort memberProjectionPort;

	@Override
	@Transactional
	public void sync(SyncMemberProjectionCommand command) {
		log.info(
				"MemberProjectionCommandService : sync : Member Projection 동기화 시작 - memberUuid={}",
				command.memberUuid()
		);

		MemberStatus memberStatus = MemberStatus.from(command.memberStatus());
		MemberProjection projection = memberProjectionPort.findByMemberUuid(command.memberUuid())
				.orElseGet(() -> MemberProjection.create(
						command.memberUuid(),
						command.nickname(),
						command.profileImage(),
						memberStatus
				));
		projection.sync(command.nickname(), command.profileImage(), memberStatus);
		memberProjectionPort.save(projection);

		log.info(
				"MemberProjectionCommandService : sync : Member Projection 동기화 완료 - memberUuid={}",
				command.memberUuid()
		);
	}
}
