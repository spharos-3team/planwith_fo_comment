package com.planwith.planwith_fo_comment.adapter.out.persistence.memberprojection;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentMemberProjectionJpaRepository extends JpaRepository<CommentMemberProjectionJpaEntity, UUID> {

	List<CommentMemberProjectionJpaEntity> findByMemberUuidIn(Collection<UUID> memberUuids);
}
