package com.planwith.planwith_fo_comment.adapter.out.persistence.likeprojection;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeProjectionJpaRepository extends JpaRepository<CommentLikeProjectionJpaEntity, UUID> {
}
