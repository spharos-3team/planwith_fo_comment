package com.planwith.planwith_fo_comment.adapter.out.persistence.storyprojection;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface CommentStoryProjectionJpaRepository extends JpaRepository<CommentStoryProjectionJpaEntity, UUID> {

	List<CommentStoryProjectionJpaEntity> findByStoryUuidIn(Collection<UUID> storyUuids);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from CommentStoryProjectionJpaEntity s where s.storyUuid = :storyUuid")
	Optional<CommentStoryProjectionJpaEntity> findByStoryUuidForUpdate(@Param("storyUuid") UUID storyUuid);
}
