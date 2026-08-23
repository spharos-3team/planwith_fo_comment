package com.planwith.planwith_fo_comment.adapter.out.persistence.reportprojection;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentReportProjectionJpaRepository extends JpaRepository<CommentReportProjectionJpaEntity, UUID> {
}
