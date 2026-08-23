package com.planwith.planwith_fo_comment.adapter.out.persistence.storyprojection;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.application.port.out.StoryProjectionPort;
import com.planwith.planwith_fo_comment.domain.storyprojection.StoryProjection;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoryProjectionPersistenceAdapter implements StoryProjectionPort {

	private final CommentStoryProjectionJpaRepository storyProjectionJpaRepository;

	@Override
	public void save(StoryProjection storyProjection) {
		storyProjectionJpaRepository.save(CommentStoryProjectionJpaEntity.builder()
				.storyUuid(storyProjection.getStoryUuid())
				.ownerMemberUuid(storyProjection.getOwnerMemberUuid())
				.commentEnabled(storyProjection.isCommentEnabled())
				.storyStatus(storyProjection.getStoryStatus())
				.sourceVersion(storyProjection.getSourceVersion())
				.synchronizedAt(storyProjection.getSynchronizedAt())
				.build());
	}

	@Override
	public Optional<StoryProjection> findByStoryUuid(UUID storyUuid) {
		return storyProjectionJpaRepository.findById(storyUuid)
				.map(this::toDomain);
	}

	@Override
	public Optional<StoryProjection> findByStoryUuidForUpdate(UUID storyUuid) {
		return storyProjectionJpaRepository.findByStoryUuidForUpdate(storyUuid)
				.map(this::toDomain);
	}

	@Override
	public List<StoryProjection> findByStoryUuids(Collection<UUID> storyUuids) {
		if (storyUuids.isEmpty()) {
			return List.of();
		}
		return storyProjectionJpaRepository.findByStoryUuidIn(storyUuids).stream()
				.map(this::toDomain)
				.toList();
	}

	private StoryProjection toDomain(CommentStoryProjectionJpaEntity entity) {
		return StoryProjection.restore(
				entity.getStoryUuid(),
				entity.getOwnerMemberUuid(),
				entity.isCommentEnabled(),
				entity.getStoryStatus(),
				entity.getSourceVersion(),
				entity.getSynchronizedAt()
		);
	}
}
