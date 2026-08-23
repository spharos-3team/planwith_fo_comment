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
import com.planwith.planwith_fo_comment.domain.comment.Comment;
import com.planwith.planwith_fo_comment.domain.comment.CommentStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements CommentCommandPort, CommentQueryPort {

	private final CommentJpaRepository commentJpaRepository;
	private final CommentMemberProjectionJpaRepository memberProjectionJpaRepository;

	@Override
	public void save(Comment comment) {
		commentJpaRepository.save(CommentPersistenceMapper.toEntity(comment));
	}

	@Override
	public Optional<Comment> findByUuid(UUID commentUuid) {
		return commentJpaRepository.findById(commentUuid)
				.map(CommentPersistenceMapper::toDomain);
	}

	@Override
	public Optional<CommentQueryResult> findActiveByUuid(UUID commentUuid) {
		return commentJpaRepository.findById(commentUuid)
				.filter(entity -> entity.getStatus() == CommentStatus.ACTIVE)
				.map(entity -> toQueryResult(entity, findProjectionMap(Set.of(entity.getMemberUuid()))));
	}

	@Override
	public List<CommentQueryResult> findActiveByStoryUuid(UUID storyUuid) {
		List<CommentJpaEntity> comments = commentJpaRepository
				.findByStoryUuidAndStatusOrderByCreatedAtAsc(storyUuid, CommentStatus.ACTIVE);
		Set<UUID> memberUuids = comments.stream()
				.map(CommentJpaEntity::getMemberUuid)
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
			CommentJpaEntity entity,
			Map<UUID, CommentMemberProjectionJpaEntity> projections
	) {
		CommentMemberProjectionJpaEntity projection = projections.get(entity.getMemberUuid());
		return new CommentQueryResult(
				entity.getCommentUuid(),
				entity.getStoryUuid(),
				entity.getMemberUuid(),
				projection == null ? null : projection.getNickname(),
				projection == null ? null : projection.getProfileImage(),
				projection == null ? null : projection.getMemberStatus(),
				entity.getContent(),
				entity.getLikeCount(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
