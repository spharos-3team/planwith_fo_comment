package com.planwith.planwith_fo_comment.adapter.out.persistence.likeprojection;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.application.port.out.CommentLikeProjectionPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentLikeProjectionPersistenceAdapter implements CommentLikeProjectionPort {

	private final CommentLikeProjectionJpaRepository commentLikeProjectionJpaRepository;

	@Override
	public boolean existsByLikeUuid(UUID likeUuid) {
		return commentLikeProjectionJpaRepository.existsById(likeUuid);
	}

	@Override
	public void save(UUID likeUuid, UUID commentUuid, UUID memberUuid) {
		commentLikeProjectionJpaRepository.save(CommentLikeProjectionJpaEntity.builder()
				.likeUuid(likeUuid)
				.commentUuid(commentUuid)
				.memberUuid(memberUuid)
				.createdAt(Instant.now())
				.build());
	}

	@Override
	public boolean deleteByLikeUuid(UUID likeUuid) {
		if (!commentLikeProjectionJpaRepository.existsById(likeUuid)) {
			return false;
		}
		commentLikeProjectionJpaRepository.deleteById(likeUuid);
		return true;
	}
}
