package com.planwith.planwith_fo_comment.adapter.out.persistence.storyprojection;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentStoryProjectionJpaRepository extends JpaRepository<CommentStoryProjectionJpaEntity, UUID> {

	List<CommentStoryProjectionJpaEntity> findByStoryUuidIn(Collection<UUID> storyUuids);
}
