package com.planwith.planwith_fo_comment.adapter.out.persistence.memberprojection;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.application.port.out.MemberProjectionPort;
import com.planwith.planwith_fo_comment.domain.memberprojection.MemberProjection;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberProjectionPersistenceAdapter implements MemberProjectionPort {

	private final CommentMemberProjectionJpaRepository memberProjectionJpaRepository;

	@Override
	public void save(MemberProjection memberProjection) {
		memberProjectionJpaRepository.save(CommentMemberProjectionJpaEntity.builder()
				.memberUuid(memberProjection.getMemberUuid())
				.nickname(memberProjection.getNickname())
				.profileImage(memberProjection.getProfileImage())
				.memberStatus(memberProjection.getMemberStatus())
				.sourceVersion(memberProjection.getSourceVersion())
				.synchronizedAt(memberProjection.getSynchronizedAt())
				.build());
	}

	@Override
	public Optional<MemberProjection> findByMemberUuid(UUID memberUuid) {
		return memberProjectionJpaRepository.findById(memberUuid)
				.map(entity -> MemberProjection.restore(
						entity.getMemberUuid(),
						entity.getNickname(),
						entity.getProfileImage(),
						entity.getMemberStatus(),
						entity.getSourceVersion(),
						entity.getSynchronizedAt()
				));
	}
}
