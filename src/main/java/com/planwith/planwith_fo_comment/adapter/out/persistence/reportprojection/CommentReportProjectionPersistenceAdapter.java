package com.planwith.planwith_fo_comment.adapter.out.persistence.reportprojection;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.application.port.out.CommentReportProjectionPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentReportProjectionPersistenceAdapter implements CommentReportProjectionPort {

	private final CommentReportProjectionJpaRepository commentReportProjectionJpaRepository;

	@Override
	public boolean existsByReportUuid(UUID reportUuid) {
		return commentReportProjectionJpaRepository.existsById(reportUuid);
	}

	@Override
	public void save(UUID reportUuid, UUID commentUuid) {
		commentReportProjectionJpaRepository.save(CommentReportProjectionJpaEntity.builder()
				.reportUuid(reportUuid)
				.commentUuid(commentUuid)
				.createdAt(Instant.now())
				.build());
	}
}
