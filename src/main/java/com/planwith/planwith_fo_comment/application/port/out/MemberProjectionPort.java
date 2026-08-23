package com.planwith.planwith_fo_comment.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.memberprojection.MemberProjection;

public interface MemberProjectionPort {

	void save(MemberProjection memberProjection);

	Optional<MemberProjection> findByMemberUuid(UUID memberUuid);

	Optional<MemberProjection> findByMemberUuidForUpdate(UUID memberUuid);
}
