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
import com.planwith.planwith_fo_comment.adapter.out.persistence.storyprojection.CommentStoryProjectionJpaEntity;
import com.planwith.planwith_fo_comment.adapter.out.persistence.storyprojection.CommentStoryProjectionJpaRepository;
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
	private final CommentStoryProjectionJpaRepository storyProjectionJpaRepository;

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
				.map(entity -> toQueryResult(
						entity,
						findMemberProjectionMap(Set.of(entity.getMemberUuid())),
						findStoryProjectionMap(Set.of(entity.getStoryUuid()))
				));
	}

	@Override
	public List<CommentQueryResult> findActiveByStoryUuid(UUID storyUuid) {
		List<StoryCommentJpaEntity> comments = storyCommentJpaRepository
				.findVisibleListByStoryUuid(storyUuid, ModerationStatus.VISIBLE);
		Set<UUID> memberUuids = comments.stream()
				.map(StoryCommentJpaEntity::getMemberUuid)
				.collect(Collectors.toSet());
		Set<UUID> storyUuids = comments.stream()
				.map(StoryCommentJpaEntity::getStoryUuid)
				.collect(Collectors.toSet());
		Map<UUID, CommentMemberProjectionJpaEntity> memberProjections = findMemberProjectionMap(memberUuids);
		Map<UUID, CommentStoryProjectionJpaEntity> storyProjections = findStoryProjectionMap(storyUuids);
		return comments.stream()
				.map(entity -> toQueryResult(entity, memberProjections, storyProjections))
				.toList();
	}

	private Map<UUID, CommentMemberProjectionJpaEntity> findMemberProjectionMap(Set<UUID> memberUuids) {
		if (memberUuids.isEmpty()) {
			return Map.of();
		}
		return memberProjectionJpaRepository.findByMemberUuidIn(memberUuids).stream()
				.collect(Collectors.toMap(CommentMemberProjectionJpaEntity::getMemberUuid, Function.identity()));
	}

	private Map<UUID, CommentStoryProjectionJpaEntity> findStoryProjectionMap(Set<UUID> storyUuids) {
		if (storyUuids.isEmpty()) {
			return Map.of();
		}
		return storyProjectionJpaRepository.findByStoryUuidIn(storyUuids).stream()
				.collect(Collectors.toMap(CommentStoryProjectionJpaEntity::getStoryUuid, Function.identity()));
	}

	private CommentQueryResult toQueryResult(
			StoryCommentJpaEntity entity,
			Map<UUID, CommentMemberProjectionJpaEntity> memberProjections,
			Map<UUID, CommentStoryProjectionJpaEntity> storyProjections
	) {
		CommentMemberProjectionJpaEntity memberProjection = memberProjections.get(entity.getMemberUuid());
		CommentStoryProjectionJpaEntity storyProjection = storyProjections.get(entity.getStoryUuid());
		return new CommentQueryResult(
				entity.getCommentUuid(),
				entity.getStoryUuid(),
				entity.getMemberUuid(),
				entity.getParentCommentUuid(),
				memberProjection == null ? null : memberProjection.getNickname(),
				memberProjection == null ? null : memberProjection.getProfileImage(),
				memberProjection == null || memberProjection.getMemberStatus() == null
						? null
						: memberProjection.getMemberStatus().name(),
				entity.getCommentContent(),
				entity.getCommentLikeCount(),
				entity.getReportCount(),
				storyProjection == null ? null : storyProjection.getOwnerMemberUuid(),
				storyProjection == null ? null : storyProjection.isCommentEnabled(),
				storyProjection == null || storyProjection.getStoryStatus() == null
						? null
						: storyProjection.getStoryStatus().name(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getDeletedAt() != null
		);
	}
}
