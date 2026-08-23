package com.planwith.planwith_fo_comment.adapter.out.persistence.comment;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.adapter.out.persistence.memberprojection.CommentMemberProjectionJpaEntity;
import com.planwith.planwith_fo_comment.adapter.out.persistence.memberprojection.CommentMemberProjectionJpaRepository;
import com.planwith.planwith_fo_comment.application.port.out.CommentCommandPort;
import com.planwith.planwith_fo_comment.application.port.out.CommentQueryPort;
import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.domain.comment.ModerationStatus;
import com.planwith.planwith_fo_comment.domain.comment.StoryComment;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements CommentCommandPort, CommentQueryPort {

	private final StoryCommentJpaRepository storyCommentJpaRepository;
	private final CommentMemberProjectionJpaRepository memberProjectionJpaRepository;

	@Override
	public void save(StoryComment comment) {
		StoryCommentJpaEntity entity = storyCommentJpaRepository.findByCommentUuid(comment.getCommentUuid())
				.orElseGet(StoryCommentJpaEntity::new);
		StoryCommentPersistenceMapper.copyToEntity(comment, entity);
		StoryCommentJpaEntity saved = storyCommentJpaRepository.save(entity);
		comment.assignCommentId(saved.getCommentId());
	}

	@Override
	public Optional<StoryComment> findByUuid(UUID commentUuid) {
		return storyCommentJpaRepository.findByCommentUuid(commentUuid)
				.map(StoryCommentPersistenceMapper::toDomain);
	}

	@Override
	public Optional<CommentQueryResult> findActiveByUuid(UUID commentUuid) {
		return storyCommentJpaRepository.findByCommentUuid(commentUuid)
				.filter(entity -> entity.getDeletedAt() == null)
				.filter(entity -> entity.getModerationStatus() == ModerationStatus.VISIBLE)
				.map(entity -> toQueryResult(entity, findProjectionMap(Set.of(entity.getMemberUuid()))));
	}

	@Override
	public List<CommentQueryResult> findActiveByStoryUuid(UUID storyUuid) {
		List<StoryCommentJpaEntity> comments = storyCommentJpaRepository
				.findByStoryUuidAndDeletedAtIsNullAndModerationStatusOrderByCreatedAtAsc(
						storyUuid,
						ModerationStatus.VISIBLE
				);
		Set<UUID> memberUuids = comments.stream()
				.map(StoryCommentJpaEntity::getMemberUuid)
				.collect(Collectors.toSet());
		Map<UUID, CommentMemberProjectionJpaEntity> projections = findProjectionMap(memberUuids);
		return comments.stream()
				.map(entity -> toQueryResult(entity, projections))
				.toList();
	}

	private Map<UUID, CommentMemberProjectionJpaEntity> findProjectionMap(Set<UUID> memberUuids) {
		if (memberUuids.isEmpty()) {
			return Map.of();
		}
		return memberProjectionJpaRepository.findByMemberUuidIn(memberUuids).stream()
				.collect(Collectors.toMap(CommentMemberProjectionJpaEntity::getMemberUuid, Function.identity()));
	}

	private CommentQueryResult toQueryResult(
			StoryCommentJpaEntity entity,
			Map<UUID, CommentMemberProjectionJpaEntity> projections
	) {
		CommentMemberProjectionJpaEntity projection = projections.get(entity.getMemberUuid());
		return new CommentQueryResult(
				entity.getCommentUuid(),
				entity.getStoryUuid(),
				entity.getMemberUuid(),
				entity.getParentCommentUuid(),
				projection == null ? null : projection.getNickname(),
				projection == null ? null : projection.getProfileImage(),
				projection == null || projection.getMemberStatus() == null
						? null
						: projection.getMemberStatus().name(),
				entity.getCommentContent(),
				entity.getCommentLikeCount(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
