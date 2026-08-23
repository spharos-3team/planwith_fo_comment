package com.planwith.planwith_fo_comment.adapter.out.persistence.memberprojection;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface CommentMemberProjectionJpaRepository extends JpaRepository<CommentMemberProjectionJpaEntity, UUID> {

	List<CommentMemberProjectionJpaEntity> findByMemberUuidIn(Collection<UUID> memberUuids);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select m from CommentMemberProjectionJpaEntity m where m.memberUuid = :memberUuid")
	Optional<CommentMemberProjectionJpaEntity> findByMemberUuidForUpdate(
			@Param("memberUuid") UUID memberUuid
	);
}
